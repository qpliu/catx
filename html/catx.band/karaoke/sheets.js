class Sheets{
    constructor(where){
	this.span = document.createElement("span");
	this.span.onkeydown = (ev)=>this.onkeydown(ev);
	this.span.style = "position:absolute;left:0;top:0;width:100vw;height:100vh;z-index:-1;";
	where.appendChild(this.span);
	this.pages = []
	document.onkeydown = e=>this.keypress(e);
	new ResizeObserver(()=>this.updatePages()).observe(this.span);
    }
    reset(name){
	if (name!=this.name){
	    this.name = name
	    this.span.innerHTML = "";
	    this.pages = []
	    this.page_l = 0;
	    this.page_r = 1;
	}
    }
    keypress(e){
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
	if (nextPage!=undefined && nextPage>=0 && this.pages.length>nextPage && this.pages[nextPage].complete)
	    if (this.pages[nextPage].naturalWidth==0)
		this.pages[nextPage].src = this.getPageUrl(nextPage);
	    else if (nextPage&1)
		this.page_r = nextPage;
	    else
		this.page_l = nextPage;
	this.updatePages();
    }
    getPageUrl(page){
	return encodeURI("/sheet_music/"+this.name+"_"+settings.who+"_"+(page+1)+".svg?t="+new Date().getTime());
    }
    updatePages(){
	if (!this.enabled)
	    return;
	while (this.pages.length<=Math.max(this.page_l,this.page_r)+1){
	    const page=this.pages.length;
	    const img=document.createElement("img");
	    if (page&1)
		img.style = "display:none;position:absolute;right:0;bottom:0;";
	    else
		img.style = "display:none;position:absolute;left:0;bottom:0;";
	    img.src = this.getPageUrl(page);
	    this.span.appendChild(img);
	    this.pages[page] = img;
	}
	const rect=this.span.getBoundingClientRect();
	const height=Math.min(rect.height,rect.width*11/8.5/2);
	const width=height*8.5/11;
	for (let page=0; page<this.pages.length; page++){
	    this.pages[page].style.width = width+"px";
	    this.pages[page].style.height = height+"px";
	    this.pages[page].style.display = page==this.page_l||page==this.page_r?"block":"none";
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
	this.updatePages();
    }
}
