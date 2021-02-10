class Background{
    constructor(where){
	this.bg_div = where;
	this.bgtext = document.createElement("span");
	this.bgtext.style = "font-size:45vh;color:#0f0;opacity:.5;";
	where.appendChild(this.bgtext);
	this.images = [];
    }
    setEnabled(enabled){
	this.enabled = enabled;
	this.bg_div.style.display = enabled?"flex":"none";
    }
    reset(startTime,repeat,songLength){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.index = 0;
	this.events = [];
	this.imgindex = 0;
	this.bgtext.style.display = "none";
	for (const img of this.images)
	    img.style.display = "none";
    }
    addEvent(t,what){
	if (what.slice(0,4)=="img="){
	    if (this.images.length==this.imgindex){
		const img=document.createElement("img");
		img.style = "opacity:.25;height:55vh;display:none;";
		this.images.push(img);
		this.bg_div.appendChild(img);
	    }
	    const img=this.images[this.imgindex++];
	    img.src = what.slice(4);
	    this.events.push({t:t,img:img});
	}else
	    this.events.push({t:t,what:what});
    }
    animate(now){
	if (!this.enabled)
	    return;
	if (this.repeat && now>=this.startTime+this.songLength){
	    this.startTime += Math.floor((now-this.songLength-this.startTime)/this.repeat+1)*this.repeat;
	    this.index = 0;
	}
	for (; this.index<this.events.length; this.index++){
	    const e=this.events[this.index];
	    const t=this.startTime+e.t;
	    if (t>=now)
		break;
	    this.bgtext.innerHTML = "";
	    this.bgtext.style.display = "none";
	    for (const img of this.images)
		img.style.display = "none";
	    if (e.img)
		e.img.style.display = "block";
	    else{
		this.bgtext.innerHTML = e.what;
		this.bgtext.style.display = "block";
	    }
	}
    }
}
