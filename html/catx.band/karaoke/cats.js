class Cats{
    constructor(where){
	this.cats = [];
	this.makeCat(where,"../cat/cat-${frame}.png",40,8,"position:absolute;left:0;top:0;width:15vw;z-index:-1;");
	this.makeCat(where,"../cat/fatcat-${frame}.png",8,2,"position:absolute;left:82vw;top:0;width:18vw;z-index:-1;");
	this.makeCat(where,"../cat/graycat-${frame}.png",4,1,"position:absolute;left:15vw;top:0;width:15vw;z-index:-1;");
	this.makeCat(where,"../cat/spin-${frame}.png",18,2,"position:absolute;left:30vw;top:0;width:15vw;z-index:-1;");
	this.makeCat(where,"../cat/pink-${frame}.png",8,2,"position:absolute;left:55vw;top:0;width:18vw;z-index:-1;");
	this.makeCat(where,"../cat/jump-${frame}.png",8,4,"position:absolute;left:70vw;top:0;width:15vw;z-index:-1;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:-9vw;top:-5vw;width:20vw;z-index:-3;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:2vw;top:-5vw;width:20vw;z-index:-2;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:13vw;top:-5vw;width:20vw;z-index:-3;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:24vw;top:-5vw;width:20vw;z-index:-2;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:35vw;top:-5vw;width:20vw;z-index:-3;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:46vw;top:-5vw;width:20vw;z-index:-2;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:58vw;top:-5vw;width:20vw;z-index:-3;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:69vw;top:-5vw;width:20vw;z-index:-2;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:80vw;top:-5vw;width:20vw;z-index:-3;");
	this.makeCat(where,"../cat/tree-${frame}.png",52,4,"position:absolute;left:91vw;top:-5vw;width:20vw;z-index:-2;");
	this.makeCat(where,"../cat/wave-${frame}.jpg",15,4,"position:absolute;left:0;top:-10vw;width:34vw;z-index:-4;");
	this.makeCat(where,"../cat/wave-${frame}.jpg",15,4,"position:absolute;left:34vw;top:-10vw;width:34vw;z-index:-4;");
	this.makeCat(where,"../cat/wave-${frame}.jpg",15,4,"position:absolute;left:68vw;top:-10vw;width:34vw;z-index:-4;");
    }
    makeCat(where,src,nframes,beats,style){
	frames = [];
	for (let frame=0; frame<nframes; frame++){
	    const img=document.createElement("img");
	    img.src = eval("`"+src+"`");
	    img.style = style;
	    where.appendChild(img);
	    frames[frame] = img;
	    if (frame!=0)
		img.style.visibility = "hidden";
	}
	this.cats.push({frames:frames,beats:beats,currentFrame:0,beat:0});
    }
    setEnabled(enabled){
	this.enabled = enabled;
	const display=enabled?"block":"none";
	for (const cat of this.cats)
	    for (const frame of cat.frames)
		frame.style.display = display;
    }
    increment(){
	for (const cat of this.cats)
	    cat.beat = (cat.beat+1)%cat.beats;
    }
    animate(x){
	if (this.enabled)
	    for (const cat of this.cats){
		cat.frames[cat.currentFrame].style.visibility = "hidden";
		cat.currentFrame = (Math.floor((cat.beat+x)*cat.frames.length/cat.beats)%cat.frames.length+cat.frames.length)%cat.frames.length;
		cat.frames[cat.currentFrame].style.visibility = "visible";
	    }
    }
    reset(){
	for (const cat of this.cats)
	    cat.beat = 0;
    }
}
