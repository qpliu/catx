class Background{
    constructor(where){
	this.bgtext = document.createElement("span");
	this.bgtext.style = "font-size:45vh;margin-top:10vh;color:#0f0;opacity:.5;display:none;";
	where.appendChild(this.bgtext);
	this.bgimg = document.createElement("img");
	this.bgimg.style = "opacity:.25;margin-left:auto;margin-right:auto;height:65vh;display:none;";
	where.appendChild(this.bgimg);
    }
    setEnabled(enabled){
	this.enabled = enabled;
	const display=enabled?"block":"none";
	this.bgtext.style.display = display;
	this.bgimg.style.display = display;
    }
    reset(startTime,repeat,songLength){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.index = 0;
	this.events = [];
	if (this.bgtext!=undefined){
	    this.bgimg.style.display = "none";
	    this.bgtext.style.display = "none";
	}
    }
    addEvent(t,what){
	this.events.push({t:t,what:what});
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
	    this.bgimg.style.display = "none";
	    this.bgtext.style.display = "none";
	    this.bgimg.src = "";
	    this.bgtext.innerHTML = "";
	    if (e.what.slice(0,4)=="img="){
		this.bgimg.src = e.what.slice(4);
		this.bgimg.style.display = "block";
	    }else{
		this.bgtext.innerHTML = e.what;
		this.bgtext.style.display = "block";
	    }
	}
    }
}
