class BeatCounter{
    constructor(cats,where){
	this.cats = cats;
	this.span = where;
    }
    reset(startTime,repeat,songLength){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.index = 0;
	this.events = [];
	this.lastTime = undefined;
    }
    addEvent(t,what){
	this.events.push({t:t,what:what});
    }
    animate(now){
	if (this.repeat && now>=this.startTime+this.songLength){
	    this.startTime += Math.floor((now-this.startTime)/this.repeat)*this.repeat;
	    this.index = 0;
	}
	for (; this.index<this.events.length; this.index++){
	    const e=this.events[this.index];
	    const t=this.startTime+e.t;
	    if (t>=now){
		if (this.lastTime!=undefined)
		    this.cats.animate((now-this.lastTime)/(t-this.lastTime));
		return;
	    }
	    this.lastTime = t;
	    this.cats.increment();
	    this.span.innerHTML = this.events[this.index].what;
	}
	if (now<this.startTime+this.songLength)
	    this.cats.animate((now-this.lastTime)/(this.startTime+this.songLength-this.lastTime));
    }
}
