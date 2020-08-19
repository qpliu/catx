class Tones{
    constructor(where){
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
    setSpeaker(value){
	settings.speaker = value;
	settings.makeEverythingAgree();
	this.speakerOn.style.display = settings.speaker?"block":"none";
	this.speakerOff.style.display = !settings.speaker?"block":"none";
    }
    reset(startTime,repeat,songLength){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.index = 0;
	this.events = [];
	this.bendEvents = [];
	for (let ch=0; ch<16; ch++)
	    this.bendEvents[ch] = [];
    }
    addBendEvent(e){
	this.bendEvents[e.ch].push(e);
    }
    addEvent(e){
	this.events.push(e);
    }
    makeBeep(buffer,e,now){
	let a=0;
	let bend=0;
	const w=2*Math.PI/buffer.sampleRate*440;
	const ww=Math.log(2)/12;
	const array=buffer.getChannelData(0);
	let bendIndex=binarySearch(this.bendEvents[e.ch],(x)=>x.t>e.t);
	const ad=Math.max(.02/buffer.sampleRate,8/array.length);
	for (let t=0; t<array.length; t++){
	    while (bendIndex<this.bendEvents[e.ch].length && (this.bendEvents[e.ch][bendIndex].t-now)*buffer.sampleRate<t*1000)
		bend = 24/8191*this.bendEvents[e.ch][bendIndex++].bend;
	    array[t] = .25*Math.min(1,Math.min(2*t,array.length-t)*ad)*Math.sin(a);
	    a += w*Math.exp((e.note+bend-69)*ww);
	}
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
	    if (settings.speaker && t+e.duration>now){
		const buffer=audioContext.createBuffer(1,audioContext.sampleRate*(t+e.duration-now)/1000,audioContext.sampleRate);
		this.makeBeep(buffer,e,now-this.startTime);
		const source=audioContext.createBufferSource();
		source.buffer = buffer;
		source.connect(audioContext.destination);
		source.start();
	    }
	}
    }
}
