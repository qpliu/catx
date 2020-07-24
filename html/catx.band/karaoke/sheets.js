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
	    this.who = undefined;
	}
	this.checkWho();
    }
    checkWho(){
	if (settings.who!=this.who){
	    this.who = settings.who;
	    this.span.innerHTML = "";
	    this.pages = []
	    this.page_l = 0;
	    this.page_r = 1;
	    this.old_page_l = 0;
	    this.old_page_r = 1;
	    this.animateStart = 0;
	    this.measuresInSong = 0;
	}
	this.loadPages();
    }
    addBeatEvent(time,beat){
	const i=beat.indexOf(":");
	if (i!=-1)
	    this.measureEvents.push({t:time,m:Number(beat.slice(0,i))});
	const j=beat.indexOf("/");
	if (j!=-1)
	    this.measuresInSong = Number(beat.slice(j+1));
    }
    turnPage(dir){
	this.old_page_l = this.page_l;
	this.old_page_r = this.page_r;
	if (dir<0)
	    if (this.page_l<this.page_r){
		if (this.page_l>0)
		    this.page_r = this.page_l-1;
	    }else if (this.page_r>0)
		this.page_l = this.page_r-1;
	    else{
		this.page_l = 0;
		this.page_r = 1;
	    }
	if (dir>0)
	    if (this.page_l<this.page_r){
		if (this.page_r<this.pages.length-1)
		    this.page_l = this.page_r+1;
	    }else if (this.page_l<this.pages.length-1)
		this.page_r = this.page_l+1;
	this.animateStart = new Date().getTime();
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
	let turns=this.pageTurns[this.who];
	if (!turns && this.pages.length>2 && this.measuresInSong){
	    turns = [];
	    for (let p=1; p<this.pages.length; p++)
		turns.push([(p+.5)/(this.pages.length-.5)*this.measuresInSong,1]);
	}
	if (turns){
	    let p=[0,1];
	    let lr=0;
	    for (const turn of turns)
		if (turn[0]<=measureNumber){
		    p[lr] = p[lr^1]+turn[1];
		    lr ^= 1;
		}
	    if (p[0]!=this.page_l || p[1]!=this.page_r){
		this.old_page_l = this.page_l;
		this.old_page_r = this.page_r;
		this.page_l = p[0];
		this.page_r = p[1];
		this.animateStart = new Date().getTime();
	    }
	}
    }
    keypress(e){
	if (!this.enabled || !this.name)
	    return;
	if (e.keyCode==37 || e.keyCode==8 || e.keyCode==33)
	    this.turnPage(-1);
	if (e.keyCode==39 || e.keyCode==32 || e.keyCode==34)
	    this.turnPage(1);
    }
    loadPages(){
	const page=this.pages.length;
	const loading=document.createElement("img");
	loading.style = "display:none;position:absolute;bottom:100%;background-color:#fff;";
	loading.src = encodeURI("/sheet_music/"+this.name+"/"+this.who+"/"+(page+1)+".svg?t="+new Date().getTime());
	loading.onload = ()=>{
	    this.pages[page] = loading;
	    this.span.appendChild(loading);
	    this.loadPages();
	};
    }
    animate(time){
	if (!this.enabled || !this.name)
	    return;
	if (isPlaying)
	    this.gotoPage(time);
	const rect=this.span.getBoundingClientRect();
	const height=Math.min(rect.height,rect.width*9/8/2);
	const width=height*8/9;
	const a=Math.min((new Date().getTime()-this.animateStart)/5,100);
	for (let page=0; page<this.pages.length; page++){
	    const s=this.pages[page].style;
	    s.width = width+"px";
	    s.height = height+"px";
	    s.left = null;
	    s.right = null;
	    s.display = "block";
	    if (page==this.page_l){
		s.bottom = Math.sign(this.page_l-this.old_page_l)*(a-100)+"%";
		s.left = 0;
	    }else if (page==this.page_r){
		s.bottom = Math.sign(this.page_r-this.old_page_r)*(a-100)+"%";
		s.right = 0;
	    }else if (page==this.old_page_l && a<100){
		s.bottom = Math.sign(this.page_l-this.old_page_l)*(a+10)+"%";
		s.left = 0;
	    }else if (page==this.old_page_r && a<100){
		s.bottom = Math.sign(this.page_r-this.old_page_r)*(a+10)+"%";
		s.right = 0;
	    }else
		s.display = "none";
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
	this.checkWho();
    }
}
