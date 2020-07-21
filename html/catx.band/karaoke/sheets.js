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
	}
    }
    addBeatEvent(time,beat){
	const i=beat.indexOf(":");
	if (i!=-1)
	    this.measureEvents.push({t:time,m:Number(beat.slice(0,i))});
    }
    gotoPage(time){
	const turns=this.pageTurns[settings.who];
	if (!turns)
	    return;
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
	let page_l=0;
	let page_r=1;
	for (const turn of turns)
	    if (turn[0]<=measureNumber){
		for (let t=turn[1]; t>0; --t)
		    if (page_l<page_r)
			page_l += 2;
		    else
			page_r += 2;
		for (let t=turn[1]; t<0; t++)
		    if (page_l<page_r)
			page_r -= 2;
		    else
			page_l -= 2;
	    }
	if (page_l!=this.page_l || page_r!=this.page_r){
	    this.old_page_l = this.page_l;
	    this.old_page_r = this.page_r;
	    this.page_l = page_l;
	    this.page_r = page_r;
	    this.animateStart = new Date().getTime();
	}
    }
    keypress(e){
	if (!this.enabled || !this.name)
	    return;
	let nextPage;
	if (e.keyCode==37 || e.keyCode==8 || e.keyCode==33)
	    if (this.page_l<this.page_r)
		nextPage = this.page_r-2;
	    else
		nextPage = this.page_l-2;
	if (e.keyCode==39 || e.keyCode==32 || e.keyCode==34)
	    if (this.page_l<this.page_r)
		nextPage = this.page_l+2;
	    else
		nextPage = this.page_r+2;
	if (nextPage!=undefined && nextPage>=0 && this.pages.length>nextPage && this.pages[nextPage].complete && this.pages[nextPage].naturalWidth){
	    this.animateStart = new Date().getTime();
	    this.old_page_r = this.page_r;
	    this.old_page_l = this.page_l;
	    if (nextPage&1)
		this.page_r = nextPage;
	    else
		this.page_l = nextPage;
	}
    }
    animate(time){
	if (!this.enabled || !this.name)
	    return;
	if (isPlaying)
	    this.gotoPage(time);
	while (this.pages.length<=Math.max(this.page_l,this.page_r)+1){
	    const page=this.pages.length;
	    const img=document.createElement("img");
	    if (page&1)
		img.style = "display:block;position:absolute;right:0;bottom:0;background-color:#fff;";
	    else
		img.style = "display:block;position:absolute;left:0;bottom:0;background-color:#fff;";
	    img.src = encodeURI("/sheet_music/"+this.name+"_"+settings.who+"_"+(page+1)+".svg?t="+new Date().getTime());
	    this.span.appendChild(img);
	    this.pages[page] = img;
	}
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
