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
	this.lastBeat = undefined;
    }
    addEvent(e){
	this.events.push(e);
    }
    addMetronome(now){
	if (this.lastBeat)
	    for (let i=0; i<settings.metronomePattern.length; i++){
		const t=this.lastBeat[0]+(now-this.lastBeat[0])*i/settings.metronomePattern.length;
		let note;
		if (settings.metronomePattern[i]=='1')
		    note = this.lastBeat[1]=='1'?88:86;
		else if (settings.metronomePattern[i]!='.')
		    note = 84;
		else
		    continue;
		this.events.push({t:t,duration:30,note:note,bends:[],volume:settings.metronomeVolume});
	    }
    }
    addBeatEvent(time,beat){
	this.addMetronome(time);
	this.lastBeat = [time,beat.slice(beat.indexOf(":")+1)];
    }
    doneAddingEvents(){
	this.addMetronome(this.songLength);
	this.events.sort((a,b)=>a.t-b.t);
    }
    makeBeep(buffer,e,now){
	let a=0;
	let bend=0;
	const w=2*Math.PI/buffer.sampleRate*440;
	const ww=Math.log(2)/12;
	const array=buffer.getChannelData(0);
	let bendIndex=0;
	const ad=Math.max(.02/buffer.sampleRate,8/array.length);
	for (let t=0; t<array.length; t++){
	    while (bendIndex<e.bends.length && (e.bends[bendIndex].t-now)*buffer.sampleRate<t*1000)
		bend = 24/8191*e.bends[bendIndex++].bend;
	    array[t] = e.volume*Math.min(1,Math.min(2*t,array.length-t)*ad)*Math.sin(a);
	    a += w*Math.exp((e.note+bend-69)*ww);
	}
    }
    animate(now){
	if (this.repeat && now>=this.startTime+this.songLength){
	    this.startTime += Math.floor((now-this.songLength-this.startTime)/this.repeat+1)*this.repeat;
	    this.index = 0;
	}
	for (; this.index<this.events.length; this.index++){
	    const e=this.events[this.index];
	    const t=this.startTime+e.t;
	    if (t>=now)
		break;
	    if (settings.speaker && audioContext && t+e.duration>now){
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
