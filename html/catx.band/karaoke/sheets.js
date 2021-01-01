class Sheets{
    constructor(where){
	this.span = document.createElement("span");
	this.span.style = "position:absolute;left:0;top:0;width:100vw;height:100vh;z-index:-1;background-color:#000;";
	where.appendChild(this.span);
	document.onkeydown = e=>this.onkeydown(e);
	this.frames = [];
	for (let frame=0; frame<7; frame++){
	    const img=document.createElement("img");
	    img.src = "../cat/run-"+frame+".png";
	    img.style = "position:absolute;width:5vw;z-index:1;";
	    where.appendChild(img);
	    this.frames[frame] = img;
	    if (frame!=0)
		img.style.visibility = "hidden";
	}
    }
    reset(startTime,repeat,songLength,name,measureMap,measureCoordinates){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.measureEvents = [];
	this.measureMap = {};
	for (const [k,v] of Object.entries(JSON.parse(measureMap))){
	    console.log([k,v]);
	    const a=[];
	    for (const x of v.split(',')){
		const xx=x.split('-');
		if (xx.length==1)
		    a.push(xx[0]-1);
		else
		    for (let j=xx[0]; j<=xx[1]; j++)
			a.push(j-1);
	    }
	    this.measureMap[k] = a;
	}
	this.measureCoordinates = JSON.parse(measureCoordinates);
	if (name!=this.name){
	    this.name = name;
	    this.who = undefined;
	}
	this.checkWho();
    }
    map_measure_number(x){
	const a=this.measureMap[this.who]||this.measureMap['default'];
	if (!a || x<0)
	    return x;
	if (x>=a.length)
	    return a[a.length-1]+x+1-a.length;
	return a[x];
    }
    checkWho(){
	const who=!this.measureCoordinates||settings.who in this.measureCoordinates?settings.who:Object.keys(this.measureCoordinates)[0];
	if (who!=this.who){
	    this.who = who;
	    this.span.innerHTML = "";
	    this.pages = []
	    this.page_l = 0;
	    this.page_r = 1;
	    this.old_page_l = 0;
	    this.old_page_r = 1;
	    this.animateStart = 0;
	}
	this.loadPages();
    }
    addBeatEvent(time,beat){
	const i=beat.indexOf(":");
	if (i!=-1){
	    const n=Number(beat.slice(0,i));
	    if (!this.measureEvents.length || n!=this.measureEvents[this.measureEvents.length-1].m)
		this.measureEvents.push({t:time,m:n});
	}
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
    gotoPage(time,width,height,rect){
	if (this.repeat&&time>=this.songLength)
	    time -= Math.floor((time-this.songLength)/this.repeat+1)*this.repeat;
	let measureNumber;
	let ee;
	for (const e of this.measureEvents){
	    if (e.t>=time && ee!=undefined){
		measureNumber = Math.max(ee.m+(time-ee.t)/(e.t-ee.t),0);
		break;
	    }
	    ee = e;
	}
	const imn=Math.floor(measureNumber);
	const mc=this.measureCoordinates[this.who];
	if (mc){
	    const iim=this.map_measure_number(imn)-1;
	    const m=Math.max(Math.min(iim,mc.length-1),0);
	    const measure=mc[m];
	    if (measure){
		let lp=this.page_l;
		let rp=this.page_r;
		if (measure[0]&1)
		    lp = measure[0]-1;
		else
		    rp = measure[0]-1;
		if (measure[1])
		    for (let p=imn;; p++){
			const o=this.map_measure_number(p);
			if (o<0 || o>=mc.length)
			    break;
			const om=mc[o];
			if (om[0]!=measure[0]){
			    if (om[0]+measure[0]&1)
				if (om[0]&1)
				    lp = om[0]-1;
				else
				    rp = om[0]-1;
			    break;
			}
		    }
		if (rp>=this.pages.length)
		    rp = this.pages.length-2+(rp+this.pages.length&1);
		if (rp<0)
		    rp &= 1;
		if (lp>=this.pages.length)
		    lp = this.pages.length-2+(lp+this.pages.length&1);
		if (lp<0)
		    lp &= 1;
		if (lp!=this.page_l || rp!=this.page_r){
		    this.old_page_l = this.page_l;
		    this.old_page_r = this.page_r;
		    this.page_l = lp;
		    this.page_r = rp;
		    this.animateStart = new Date().getTime();
		}
		measureNumber += iim-imn-m;
		for (const f of this.frames)
		    f.style.visibility = "hidden";
		const s=this.frames[((measureNumber*28|0)%7+7)%7].style;
		s.visibility = "visible";
		s.top = (rect.height+(measure[5]+.5*(measure[5]-measure[3])-1)*height)+"px";
		const x0=(measure[2]+measureNumber*(measure[4]-measure[2]));
		if (this.page_l+1==measure[0])
		    s.right = (rect.width-x0*width)+"px";
		else if (this.page_r+1==measure[0])
		    s.right = (1-x0)*width+"px";
	    }
	}
    }
    onkeydown(e){
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
	    if (this.pages[page]==undefined){
		this.pages[page] = loading;
		this.span.appendChild(loading);
	    }
	    this.loadPages();
	};
    }
    animate(time){
	if (!this.enabled || !this.name)
	    return;
	const rect=this.span.getBoundingClientRect();
	const height=Math.min(rect.height,rect.width*9/8/2);
	const width=height*8/9;
	if (isPlaying)
	    this.gotoPage(time,width,height,rect);
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
	const display=enabled?"block":"none";
	this.span.style.display = display;
	for (const frame of this.frames)
	    frame.style.display = display;
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
