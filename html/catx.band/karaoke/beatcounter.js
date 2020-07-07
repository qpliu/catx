class BeatCounter{
    constructor(where){
	this.span = where;
    }
    reset(repeat,songLength){
	this.repeat = repeat;
	this.songLength = songLength;
	this.events = [];
    }
    addEvent(t,what){
	this.events.push({t:t,what:what});
    }
    setTime(time){
	if (this.repeat&&time>=this.songLength)
	    time -= Math.floor((time-this.songLength)/this.repeat+1)*this.repeat;
	let what="";
	let count=0;
	let lastTime=0;
	let nextTime=this.songLength;
	for (const e of this.events){
	    if (e.t>=time){
		nextTime = e.t;
		break;
	    }
	    count++;
	    what = e.what;
	    lastTime = e.t;
	}
	this.span.innerHTML = what;
	if (nextTime>lastTime)
	    cats.setBeat(count+(time-lastTime)/(nextTime-lastTime));
    }
}
