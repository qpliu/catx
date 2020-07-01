class 球球{
    constructor(where){
	this.lines = [];
	this.lineats = [];
	this.id_counter = 0;
	for (let i=0; i<2; i++){
	    this.lines[i] = document.createElement("div");
	    this.lines[i].style = "position:absolute;top:"+(85-21*i)+"vh;left:"+(20-10*i)+"vw;width:"+(80+10*i)+"vw;font-size:2vw;white-space:nowrap;";
	    where.appendChild(this.lines[i]);
	    this.lineats[i] = document.createElement("div");
	    this.lineats[i].style = "position:absolute;top:"+(85-21*i)+"vh;left:"+(15-10*i)+"vw;width:5vw;font-size:2vw;";
	    where.appendChild(this.lineats[i]);
	}
	this.lineatat = document.createElement("div");
	this.lineatat.style = "position:absolute;top:100vh;left:97.5vw;height:0;";
	where.appendChild(this.lineatat);
	this.ball = document.createElement("img");
	this.ball.style = "position:absolute;bottom:100vh;width:5vw;z-index:2;";
	this.ball.src = "../ball.png";
	where.appendChild(this.ball);
    }
    setEnabled(enabled){
	this.enabled = enabled;
	const display=enabled?"block":"none";
	for (let i=0; i<2; i++){
	    this.lines[i].style.display = display;
	    this.lineats[i].style.display = display;
	}
	this.lineatat.style.display = display;
	this.ball.style.display = display;
	if (enabled && this.startTime==undefined){
	    this.startTime = new Date().getTime();
	    this.animate(this.startTime);
	    this.startTime = undefined;
	}
    }
    reset(startTime,repeat){
	this.startTime = startTime;
	this.repeat = repeat;
	this.nextLineStart = 0;
	this.whichLine = 0;
	this.lineEvents = [];
	this.wordElement = null;
	this.lyricEvents = [];
	this.lyricIndex = 0;
	this.ballEvents = [];
	this.ball_t0 = undefined;
	this.ball_t1 = startTime;
	this.ball_x1 = 0;
	this.ball_y1 = 0;
	for (const line of this.lines)
	    line.innerHTML = "";
    }
    addEvent(t,what){
	this.lyricEvents.push({t:t,what:what});
    }
    animate(now){
	if (!this.enabled)
	    return;
	while (this.nextLineStart<now && this.lyricIndex<this.lyricEvents.length){
	    this.whichLine ^= 1;
	    let this_line_start;
	    let this_line_end;
	    let sb=""
	    while (this.lyricIndex<this.lyricEvents.length){
		const e=this.lyricEvents[this.lyricIndex];
		const t=this.startTime+e.t;
		let k=e.what;
		if (k.slice(0,1)==">"){
		    if (this_line_start!=undefined)
			break;
		    k = k.slice(1);
		}
		if (k=="@@"){
		    this.ballEvents.push([t,this.lineatat]);
		    k = "-";
		}else if (k=="@"){
		    this.ballEvents.push([t,this.lineats[this.whichLine]]);
		    k = "-";
		}
		if (k.slice(0,1)=="-")
		    k = k.slice(1);
		else if (sb.length!=0)
		    sb += "&nbsp;";
		sb += "<span style=position:relative;>";
		sb += "<span style=color:#000;>"+k+"</span>";
		sb += "<span id=id"+this.id_counter+" style=overflow:hidden;position:absolute;left:0;width:0;z-index:1;color:#f00;>"+k+"</span>";
		sb += "</span>";
		if (k.length!=0){
		    this_line_end = t;
		    if (this_line_start==undefined)
			this_line_start = t;
		    this.ballEvents.push([t,undefined,"id"+this.id_counter]);
		}
		this.lineEvents.push([t,"id"+this.id_counter]);
		this.id_counter++;
		if (this.repeat && this.lyricIndex==this.lyricEvents.length-1){
		    this.startTime += Math.max(Math.ceil((now-this.startTime-e.t)/this.repeat),1)*this.repeat;
		    this.lyricIndex = 0;
		    break;
		}
		this.lyricIndex++;
	    }
	    this.lines[this.whichLine].innerHTML = sb;
	    if (this_line_start==undefined)
		this.nextLineStart = now;
	    else
		this.nextLineStart = (2*this_line_start+this_line_end)/3;
	}
	for (; this.lineEvents.length!=0&&this.lineEvents[0][0]<now; this.lineEvents=this.lineEvents.slice(1)){
	    if (this.wordElement!=null)
		this.wordElement.style.overflow = "visible";
	    this.wordElement = document.getElementById(this.lineEvents[0][1]);
	    if (this.wordElement!=null){
		this.word_t0 = this.lineEvents[0][0];
		this.word_t1 = this.lineEvents.length>1?this.lineEvents[1][0]:now;
	    }
	}
	if (this.wordElement!=null)
	    this.wordElement.style.width = (now-this.word_t0)/(this.word_t1-this.word_t0)*100+"%";
	for (; this.lineEvents.length!=0&&this.lineEvents[0][0]<now; this.lineEvents=this.lineEvents.slice(1)){
	    const element=document.getElementById(this.lineEvents[0][1]);
	    if (element!=null)
		element.style.color = '#f00';
	}
	for (; this.ball_t1<now&&this.ballEvents.length!=0; this.ballEvents=this.ballEvents.slice(1)){
	    if (this.ballEvents[0][1]==undefined)
		this.ballEvents[0][1] = document.getElementById(this.ballEvents[0][2]);
	    const element=this.ballEvents[0][1];
	    if (element!=null){
		const rect=element.getBoundingClientRect();
		this.ball_t0 = this.ball_t1;
		this.ball_x0 = this.ball_x1;
		this.ball_y0 = this.ball_y1;
		this.ball_t1 = this.ballEvents[0][0];
		this.ball_x1 = rect.left;
		this.ball_y1 = rect.top;
	    }
	}
	if (this.ball_t0!=undefined){
	    const tt=this.ball_t1-this.ball_t0;
	    const t=Math.min((now-this.ball_t0)/tt,1);
	    const r=this.ball.getBoundingClientRect();
	    this.ball.style.left = (this.ball_x0+t*(this.ball_x1-this.ball_x0)+(r.left-r.right)/2)+"px";
	    const bounce_height=2*(r.bottom-r.top);
	    this.ball.style.top = (this.ball_y0+t*(this.ball_y1-this.ball_y0)-r.bottom+r.top-(1-4*(t-.5)*(t-.5))*bounce_height*Math.min(tt/2000,1))+"px";
	}
    }
}
