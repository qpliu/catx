class Tones{
    constructor(where){
	this.where = where;
    }
    setEnabled(enabled){
	this.enabled = enabled;
	if (enabled && this.speakerOn==undefined){
	    audioContext.resume();
	    this.speakerOn = document.createElement("img");
	    this.speakerOn.style = "position:absolute;right:6vw;top:0;width:5vw;z-index:9;"
	    this.speakerOn.onclick = ()=>this.setSpeaker(0);
	    this.speakerOn.src = "../speaker.svg";
	    this.where.appendChild(this.speakerOn);
	    this.speakerOff = document.createElement("img");
	    this.speakerOff.style = "position:absolute;right:6vw;top:0;width:5vw;z-index:9;";
	    this.speakerOff.onclick = ()=>this.setSpeaker(1);
	    this.speakerOff.src = "../speaker_off.svg";
	    this.where.appendChild(this.speakerOff);
	    this.toneOscillator = audioContext.createOscillator();
	    this.toneGain = audioContext.createGain();
	    this.toneGain.gain.value = 0;
	    this.toneGain.connect(audioContext.destination);
	    this.toneOscillator.connect(this.toneGain);
	    this.toneOscillator.start();
	}
	this.setSpeaker(settings.speaker);
    }
    setSpeaker(value){
	settings.speaker = value;
	settings.makeEverythingAgree();
	if (!settings.speaker || !this.enabled)
	    this.toneGain.gain.setValueAtTime(0,audioContext.currentTime);
	this.speakerOn.style.display = this.enabled&&settings.speaker?"block":"none";
	this.speakerOff.style.display = this.enabled&&!settings.speaker?"block":"none";
    }
    reset(startTime,repeat,songLength){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.index = 0;
	this.events = [];
    }
    addEvent(t,duration,note){
	this.events.push({t:t,duration:duration,note:note});
    }
    animate(now){
	if (!this.enabled)
	    return;
	if (this.repeat && now>=this.startTime+this.songLength){
	    this.startTime += Math.floor((now-this.startTime)/this.repeat)*this.repeat;
	    this.index = 0;
	}
	for (; this.index<this.events.length; this.index++){
	    const e=this.events[this.index];
	    const t=this.startTime+e.t;
	    if (t>=now)
		break;
	    if (settings.speaker && t+e.duration>now){
		this.toneOscillator.frequency.value = Math.exp((e.note-69)/12*Math.log(2))*440;
		this.toneGain.gain.setValueAtTime(.001,audioContext.currentTime);
		this.toneGain.gain.exponentialRampToValueAtTime(1,audioContext.currentTime+Math.min(e.duration/2500,.05));
		this.toneGain.gain.setValueAtTime(1,audioContext.currentTime+e.duration/1000-Math.min(e.duration/2500,.05));
		this.toneGain.gain.exponentialRampToValueAtTime(.001,audioContext.currentTime+e.duration/1000);
	    }
	}
    }
}
