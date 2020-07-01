class Tones{
    constructor(where){
	this.gains = [];
	this.speakerOn = document.createElement("img");
	this.speakerOn.style = "position:absolute;left:0;top:0;width:5vw;z-index:9;"
	this.speakerOn.onclick = ()=>{gotClick();this.setSpeaker(0);};
	this.speakerOn.src = "../speaker.svg";
	where.appendChild(this.speakerOn);
	this.speakerOff = document.createElement("img");
	this.speakerOff.style = "position:absolute;left:0;top:0;width:5vw;z-index:9;";
	this.speakerOff.onclick = ()=>{gotClick();this.setSpeaker(1);};
	this.speakerOff.src = "../speaker_off.svg";
	where.appendChild(this.speakerOff);
	this.setSpeaker(settings.speaker);
    }
    enable(){
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
    setSpeaker(value){
	settings.speaker = value;
	settings.makeEverythingAgree();
	this.speakerOn.style.display = settings.speaker?"block":"none";
	this.speakerOff.style.display = !settings.speaker?"block":"none";
	if (!settings.speaker)
	    for (const gain of this.gains)
		if (gain!=undefined)
		    gain.gain.setValueAtTime(0,audioContext.currentTime);
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
	if (this.repeat && now>=this.startTime+this.songLength){
	    this.startTime += Math.floor((now-this.startTime)/this.repeat)*this.repeat;
	    this.index = 0;
	}
	for (; this.index<this.events.length; this.index++){
	    const e=this.events[this.index];
	    const t=this.startTime+e.t;
	    if (t>=now)
		break;
	    if (settings.speaker && t+e.duration>now && this.gains[e.note]){
		this.gains[e.note].gain.setValueAtTime(.00001,audioContext.currentTime);
		this.gains[e.note].gain.exponentialRampToValueAtTime(.25,audioContext.currentTime+Math.min(e.duration/2500,.05));
		this.gains[e.note].gain.setValueAtTime(.25,audioContext.currentTime+e.duration/1000-Math.min(e.duration/2500,.05));
		this.gains[e.note].gain.exponentialRampToValueAtTime(.00001,audioContext.currentTime+e.duration/1000);
	    }
	}
    }
}
