class BeatCounter{
    constructor(cats,where){
	this.cats = cats;
	this.span = document.createElement("span");
	this.span.style = "font-family:monospace;font-size:8vh;color:#000;background-color:#0f0;";
	where.appendChild(this.span);
    }
    reset(startTime,repeat){
	this.startTime = startTime;
	this.repeat = repeat;
	this.index = 0;
	this.events = [];
    }
    addEvent(time,what){
	this.events.push({time:time,what:what});
    }
    animate(now){
	while (this.index<this.events.length){
	    const e=this.events[this.index];
	    const t=this.startTime+e.time;
	    if (t>=now){
		if (this.index!=0)
		    this.cats.animate(1-(t-now)/(this.events[this.index].time-this.events[this.index-1].time));
		break;
	    }
	    this.cats.increment();
	    this.span.innerHTML = this.events[this.index].what;
	    if (this.repeat && this.index==this.events.length-1){
		this.startTime += Math.ceil((now-t)/this.repeat)*this.repeat;
		this.index = 0;
	    }else
		this.index++;
	}
    }
}
