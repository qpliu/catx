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
	    this.gains = [];
	    for (let i=0; i<128; i++){
		const oscillator=audioContext.createOscillator();
		oscillator.frequency.value = Math.exp((i-69)/12*Math.log(2))*440;
		this.gains[i] = audioContext.createGain();
		this.gains[i].gain.value = 0;
		this.gains[i].connect(audioContext.destination);
		oscillator.connect(this.gains[i]);
		oscillator.start();
	    }
	}
	this.setSpeaker(settings.speaker);
    }
    setSpeaker(value){
	settings.speaker = value;
	settings.makeEverythingAgree();
	if (!settings.speaker || !this.enabled)
	    for (const gain of this.gains)
		if (gain!=undefined)
		    gain.gain.setValueAtTime(0,audioContext.currentTime);
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
    addEvent(e){
	this.events.push(e);
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
		this.gains[e.note].gain.setValueAtTime(.001,audioContext.currentTime);
		this.gains[e.note].gain.exponentialRampToValueAtTime(.25,audioContext.currentTime+Math.min(e.duration/2500,.05));
		this.gains[e.note].gain.setValueAtTime(.25,audioContext.currentTime+e.duration/1000-Math.min(e.duration/2500,.05));
		this.gains[e.note].gain.exponentialRampToValueAtTime(.001,audioContext.currentTime+e.duration/1000);
	    }
	}
    }
}
