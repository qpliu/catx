class Sheets{
    constructor(where){
	this.span = document.createElement("span");
	this.span.onkeydown = (ev)=>this.onkeydown(ev);
	this.span.style = "position:absolute;left:0;top:0;width:100vw;height:100vh;z-index:-1;background-color:#000;";
	where.appendChild(this.span);
	document.onkeydown = e=>this.keypress(e);
    }
    reset(startTime,repeat,songLength,name,pageTurns){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.measureEvents = [];
	this.pageTurns = JSON.parse(pageTurns);
	if (name!=this.name){
	    this.name = name;
	    this.span.innerHTML = "";
	    this.pages = []
	    this.page_l = 0;
	    this.page_r = 1;
	    this.old_page_l = 0;
	    this.old_page_r = 1;
	    this.animateStart = 0;
	    this.measuresInSong = 0;
	    this.loadPages();
	}
    }
    addBeatEvent(time,beat){
	const i=beat.indexOf(":");
	if (i!=-1)
	    this.measureEvents.push({t:time,m:Number(beat.slice(0,i))});
	const j=beat.indexOf("/");
	if (j!=-1)
	    this.measuresInSong = Number(beat.slice(j+1));
    }
    turnPage(lp,rp,dir){
	for (; dir<0; dir++)
	    if (lp<rp){
		if (rp>1)
		    rp -= 2;
	    }else if (lp>1)
		lp -= 2;
	for (; dir>0; --dir)
	    if (lp<rp){
		if (lp<this.pages.length-2)
		    lp += 2;
	    }else if (rp<this.pages.length-2)
		rp += 2;
	if (lp!=this.page_l || rp!=this.page_r){
	    this.old_page_l = this.page_l;
	    this.old_page_r = this.page_r;
	    this.page_l = lp;
	    this.page_r = rp;
	    this.animateStart = new Date().getTime();
	}
    }
    gotoPage(time){
	if (this.repeat&&time>=this.songLength)
	    time -= Math.floor((time-this.songLength)/this.repeat+1)*this.repeat;
	let measureNumber;
	for (const e of this.measureEvents){
	    if (e.t>=time)
		break;
	    measureNumber = e.m;
	}
	if (measureNumber==undefined)
	    return;
	let turns=this.pageTurns[settings.who];
	if (!turns && this.pages.length>2 && this.measuresInSong){
	    turns = [];
	    for (let p=1; p<this.pages.length; p++)
		turns.push([(p+.5)/(this.pages.length-.5)*this.measuresInSong,1]);
	}
	if (turns){
	    let dir=0;
	    for (const turn of turns)
		if (turn[0]<=measureNumber)
		    dir += turn[1];
	    this.turnPage(0,1,dir);
	}
    }
    keypress(e){
	if (!this.enabled || !this.name)
	    return;
	if (e.keyCode==37 || e.keyCode==8 || e.keyCode==33)
	    this.turnPage(this.page_l,this.page_r,-1);
	if (e.keyCode==39 || e.keyCode==32 || e.keyCode==34)
	    this.turnPage(this.page_l,this.page_r,1);
    }
    loaded(){
	this.pages.push(this.loading);
	this.loadPages();
    }
    loadPages(){
	const page=this.pages.length;
	this.loading = document.createElement("img");
	if (page&1)
	    this.loading.style = "display:block;position:absolute;right:0;bottom:100%;background-color:#fff;";
	else
	    this.loading.style = "display:block;position:absolute;left:0;bottom:100%;background-color:#fff;";
	this.loading.src = encodeURI("/sheet_music/"+this.name+"_"+settings.who+"_"+(page+1)+".svg?t="+new Date().getTime());
	this.span.appendChild(this.loading);
	this.loading.onload = ()=>this.loaded();
    }
    animate(time){
	if (!this.enabled || !this.name)
	    return;
	if (isPlaying)
	    this.gotoPage(time);
	const rect=this.span.getBoundingClientRect();
	const height=Math.min(rect.height,rect.width*11/8.5/2);
	const width=height*8.5/11;
	const a=Math.min((new Date().getTime()-this.animateStart)/9,55);
	const la=this.page_l*a+this.old_page_l*(55-a);
	const ra=this.page_r*a+this.old_page_r*(55-a);
	for (let page=0; page<this.pages.length; page++){
	    const s=this.pages[page].style;
	    s.width = width+"px";
	    s.height = height+"px";
	    s.bottom = (page&1?ra:la)-page*55+"%";
	}
    }
    setEnabled(enabled){
	this.enabled = enabled;
	this.span.style.display = enabled?"block":"none";
	const opacity=enabled?.2:1;
	document.getElementById("name").style.opacity = opacity;
	document.getElementById("stopImg").style.opacity = opacity;
	document.getElementById("playImg").style.opacity = opacity;
	document.getElementById("settingsImg").style.opacity = opacity;
	document.getElementById("qiuqiusImg").style.opacity = opacity;
	document.getElementById("snakesImg").style.opacity = opacity;
	document.getElementById("sheetsImg").style.opacity = opacity;
	document.getElementById("beat_span").style.opacity = opacity;
	tones.speakerOn.style.opacity = opacity;
	tones.speakerOff.style.opacity = opacity;
    }
}
