class BeatCounter{
    constructor(cats,where){
	this.cats = cats;
	this.span = where;
    }
    reset(startTime,repeat){
	this.startTime = startTime;
	this.repeat = repeat;
	this.index = 0;
	this.events = [];
	this.lastTime = undefined;
    }
    addEvent(time,what){
	this.events.push({time:time,what:what});
    }
    animate(now){
	while (this.index<this.events.length){
	    const e=this.events[this.index];
	    const t=this.startTime+e.time;
	    if (t>=now){
		if (this.lastTime!=undefined)
		    this.cats.animate((now-this.lastTime)/(t-this.lastTime));
		break;
	    }
	    this.lastTime = t;
	    this.cats.increment();
	    this.span.innerHTML = this.events[this.index].what;
	    if (this.repeat && this.index==this.events.length-1){
		this.startTime += (Math.floor((now-t)/this.repeat)+1)*this.repeat;
		this.index = 0;
	    }else
		this.index++;
	}
    }
}
