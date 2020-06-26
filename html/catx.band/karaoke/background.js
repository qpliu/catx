class Background{
    constructor(where){
	this.bgtext = document.createElement("span");
	this.bgtext.style = "font-size:45vh;margin-top:10vh;color:#0f0;opacity:.5;display:none;";
	where.appendChild(this.bgtext);
	this.bgimg = document.createElement("img");
	this.bgimg.style = "opacity:.25;margin-left:auto;margin-right:auto;height:65vh;display:none;";
	where.appendChild(this.bgimg);
    }
    reset(startTime,repeat){
	this.startTime = startTime;
	this.repeat = repeat;
	this.index = 0;
	this.events = [];
	this.bgimg.style.display = "none";
	this.bgtext.style.display = "none";
    }
    addEvent(time,what){
	this.events.push([time,what]);
    }
    animate(now){
	while (this.index<this.events.length){
	    const e=this.events[this.index];
	    const t=this.startTime+e[0];
	    if (t>now)
		break;
	    this.bgimg.style.display = "none";
	    this.bgtext.style.display = "none";
	    this.bgimg.src = "";
	    this.bgtext.innerHTML = "";
	    if (e[1].slice(0,4)=="img="){
		this.bgimg.src = e[1].slice(4);
		this.bgimg.style.display = "block";
	    }else{
		this.bgtext.innerHTML = e[1];
		this.bgtext.style.display = "block";
	    }
	    if (this.repeat && this.index==this.events.length-1){
		this.startTime += Math.ceil((now-this.startTime-e[0])/this.repeat)*this.repeat;
		this.index = 0;
	    }else
		this.index++;
	}
    }
}
