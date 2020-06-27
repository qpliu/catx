class Tones{
    constructor(audioContext,where){
	this.audioContext = audioContext;
	this.speakerOn = document.createElement("img");
	this.speakerOn.style = "visibility:hidden;position:absolute;right:6vw;top:0;width:5vw;z-index:9;"
	const thiz=this;
	this.speakerOn.onclick = function(){thiz.setSpeaker(0);};
	this.speakerOn.src = "../speaker.svg";
	where.appendChild(this.speakerOn);
	this.speakerOff = document.createElement("img");
	this.speakerOff.style = "position:absolute;right:6vw;top:0;width:5vw;z-index:9;";
	this.speakerOff.onclick = function(){thiz.setSpeaker(1);};
	this.speakerOff.src = "../speaker_off.svg";
	where.appendChild(this.speakerOff);
	this.toneOscillator = audioContext.createOscillator();
	this.toneGain = audioContext.createGain();
	this.toneGain.gain.value = 0;
	this.toneGain.connect(audioContext.destination);
	this.toneOscillator.connect(this.toneGain);
	this.toneOscillator.start();
	this.setSpeaker(settings.speaker);
    }
    setSpeaker(value){
	settings.speaker = value;
	settings.setHref();
	if (!settings.speaker)
	    this.toneGain.gain.setValueAtTime(0,this.audioContext.currentTime);
	this.speakerOn.style.visibility = settings.speaker?"visible":"hidden";
	this.speakerOff.style.visibility = settings.speaker?"hidden":"visible";
    }
    reset(startTime,repeat){
	this.startTime = startTime;
	this.repeat = repeat;
	this.index = 0;
	this.events = [];
    }
    addEvent(time,duration,note){
	this.events.push({time:time,duration:duration,note:note});
    }
    animate(now){
	while (this.index<this.events.length){
	    const e=this.events[this.index];
	    const t=this.startTime+e.time;
	    if (t>=now)
		break;
	    if (settings.speaker && t+e.duration>now){
		this.toneOscillator.frequency.value = Math.exp((e.note-69)/12*Math.log(2))*440;
		this.toneGain.gain.setValueAtTime(.001,this.audioContext.currentTime);
		this.toneGain.gain.exponentialRampToValueAtTime(1,this.audioContext.currentTime+Math.min(e.duration/2500,.05));
		this.toneGain.gain.setValueAtTime(1,this.audioContext.currentTime+e.duration/1000-Math.min(e.duration/2500,.05));
		this.toneGain.gain.exponentialRampToValueAtTime(.001,this.audioContext.currentTime+e.duration/1000);
	    }
	    if (this.repeat && this.index==this.events.length-1){
		this.startTime += (Math.floor((now-t)/this.repeat)+1)*this.repeat;
		this.index = 0;
	    }else
		this.index++;
	}
    }
}
