class Snakes{
    constructor(where){
	this.divs = [];
	this.canvases = [];
	for (let i=0; i<100; i++){
	    this.divs[i] = document.createElement("div");
	    this.divs[i].style = "position:absolute;top:20vh;color:#0f0;font-size:3vh;z-index:1;";
	    where.appendChild(this.divs[i]);
	    this.canvases[i] = document.createElement("canvas");
	    this.canvases[i].style = "position:absolute;top:20vh;";
	    where.appendChild(this.canvases[i]);
	}
	this.grayDiv = document.createElement("div");
	this.grayDiv.style = "position:absolute;top:0;left:0;width:100vw;height:20vh;background-color:#888;z-index:-1;";
	where.appendChild(this.grayDiv);
	this.staticDiv = document.createElement("div");
	this.staticDiv.style = "position:absolute;top:20vh;left:0;width:100vw;height:80vh;font-size:2vh;color:#0ff;z-index:2;user-select:none;touch-action:none;display:block;";
	this.staticDiv.onpointermove = (event)=>this.onpointermove(event);
	this.staticDiv.onpointerdown = (event)=>this.onpointerdown(event);
	this.staticDiv.onpointerup = (event)=>this.onpointerup(event);
	this.staticDiv.onpointerleave = (event)=>this.onpointerup(event);
	where.appendChild(this.staticDiv);
	this.lt = document.createElement("img");
	this.lt.style = "position:absolute;top:55vh;left:0;height:10vh;z-index:2;";
	this.lt.src = "../lt.svg";
	where.appendChild(this.lt);
	this.gt = document.createElement("img");
	this.gt.style = "position:absolute;top:55vh;right:0;height:10vh;z-index:2;";
	this.gt.src = "../gt.svg";
	where.appendChild(this.gt);
    }
    reset(startTime,repeat,songLength){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.toneEvents = [];
	this.lyricEvents = [];
	this.keysignatureEvents = [];
	this.beatEvents = [];
	this.canvasTime = 0;
	this.lastKey = undefined;
	this.lastX = 0;
	this.lastFftX = 0;
	this.scroll = 0;
	for (const canvas of this.canvases)
	    canvas.style.display = "none";
	for (const div of this.divs)
	    div.style.display = "none";
    }
    onpointermove(e){
	if (this.pointerDown!=undefined){
	    if (!isPlaying){
		this.scroll += Math.floor(this.pointerDown[0]-e.clientX);
		this.fixScroll();
	    }
	    this.pointerDown = [e.clientX,e.clientY];
	}
    }
    onpointerdown(e){
	this.pointerDown = [e.clientX,e.clientY];
    }
    onpointerup(e){
	this.pointerDown = undefined;
    }
    setEnabled(enabled){
	this.enabled = enabled;
	this.grayDiv.style.display = enabled?"block":"none";
	this.staticDiv.style.visibility = enabled?"visible":"hidden";
	this.lt.style.display = "none";
	this.gt.style.display = "none";
	for (const canvas of this.canvases)
	    canvas.style.display = "none";
	for (const div of this.divs)
	    div.style.display = "none";
	if (enabled && !this.gotMicrophone){
	    this.gotMicrophone = true;
	    navigator.mediaDevices.getUserMedia({audio:{echoCancellation:{ideal:false}}}).then(stream=>{
		const microphone=audioContext.createMediaStreamSource(stream);
		this.fft = audioContext.createAnalyser();
		this.fft.fftSize = settings.fftSize;
		this.fft.smoothingTimeConstant = 0;
		this.fft.minDecibels = -settings.microphoneSensitivity;
		this.fft.maxDecibels = this.fft.minDecibels+20;
		microphone.connect(this.fft);
		this.fft_data = new Uint8Array(this.fft.frequencyBinCount);
	    },err=>alert(err));
	}
    }
    addToneEvent(e){
	this.toneEvents.push(e);
    }
    addBeatEvent(t,what){
	this.beatEvents.push({t:t,what:what});
    }
    addLyricEvent(t,what){
	this.lyricEvents.push({t:t,what:what});
    }
    addKeysignatureEvent(e){
	this.keysignatureEvents.push({t:e.t,key:e.key});
    }
    doneAddingEvents(){
	let ti=0;
	let lastNote;
	for (const e of this.lyricEvents){
	    for (; ti<this.toneEvents.length&&this.toneEvents[ti].t<=e.t; ti++)
		lastNote = this.toneEvents[ti].note;
	    e.note = lastNote;
	}
    }
    noteToString(key,note){
	let s=[["C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B"],["C","D♭","D","E♭","E","F","G♭","G","A♭","A","B♭","B"]][key[0]<0?1:0][note%12];
	const n=(note+12-[11,6,1,8,3,10,5,0,7,2,9,4,11,6,1][key[0]+7])%12;
	if (n==(key[1]?9:0))
	    s += Math.floor(note/12)-1;
	return s;
    }
    keyContainsNoteColor(key,note){
	note = (note+12-[11,6,1,8,3,10,5,0,7,2,9,4,11,6,1][key[0]+7])%12;
	if (note==(key[1]?9:0))
	    return "#808080";
	if (note==0 || note==2 || note==4 || note==5 || note==7 || note==9 || note==11)
	    return "#404040";
    }
    drawLetters(time){
	let bestTime=Infinity;
	let bestKey=[0,0];
	for (const e of this.keysignatureEvents){
	    let t=time;
	    if (this.repeat && t>=this.songLength)
		t -= Math.floor((t-this.songLength)/this.repeat+1)*this.repeat;
	    t -= e.t;
	    if (t>=0 && t<bestTime){
		bestTime = t;
		bestKey = e.key;
	    }
	}
	if (bestKey!=this.lastKey){
	    this.lastKey = bestKey;
	    let sb="";
	    for (let note=settings.minNote+1; note<settings.maxNote; note++)
		if (this.keyContainsNoteColor(bestKey,note)){
		    const y=(settings.maxNote-note)/(settings.maxNote-settings.minNote)*80-1.1;
		    sb += "<span style=position:absolute;left:50%;top:"+y+"vh;>"+this.noteToString(bestKey,note)+"</span>"
		}
	    this.staticDiv.innerHTML = sb;
	}
    }
    drawCanvas(x){
	let t0=Math.floor(this.canvasTime-this.startTime-settings.snakeTime/2+x*settings.snakeTime/this.canvasWidth);
	let t1=Math.floor(this.canvasTime-this.startTime-settings.snakeTime/2+(x+1)*settings.snakeTime/this.canvasWidth);
	if (this.repeat && t1>=this.songLength){
	    const tt=Math.ceil((t1-this.songLength)/this.repeat)*this.repeat;
	    t0 -= tt;
	    t1 -= tt;
	}
	let lt0=Math.floor(this.canvasTime-this.startTime-3*settings.snakeTime/2+x*settings.snakeTime/this.canvasWidth);
	let lt1=Math.floor(this.canvasTime-this.startTime-3*settings.snakeTime/2+(x+1)*settings.snakeTime/this.canvasWidth);
	if (this.repeat && lt1>=this.songLength){
	    const tt=Math.ceil((lt1-this.songLength)/this.repeat)*this.repeat;
	    lt0 -= tt;
	    lt1 -= tt;
	}
	const which=Math.floor(x/this.canvasWidth);
	x -= which*this.canvasWidth;
	const div=[this.divs[which%this.divs.length],this.divs[(which+this.divs.length-1)%this.canvases.length]];
	const context=this.canvases[which%this.canvases.length].getContext("2d");
	context.globalCompositeOperation = "source-over";
	if (x==0)
	    div[1].innerHTML = "";
	let key=[0,0];
	for (const e of this.keysignatureEvents){
	    if (e.t>t0)
		break;
	    key = e.key;
	}
	context.fillStyle = "#000000";
	context.fillRect(x,0,1,this.canvasHeight);
	for (let note=settings.minNote+1; note<settings.maxNote; note++){
	    const color=this.keyContainsNoteColor(key,note);
	    if (color!=undefined){
		context.fillStyle = color;
		const y=Math.floor((settings.maxNote-note)/(settings.maxNote-settings.minNote)*this.canvasHeight);
		context.fillRect(x,y,1,1);
	    }
	}
	for (const e of this.beatEvents)
	    if (e.t>=t0 && e.t<t1){
		context.fillStyle = e.what.slice(-2)==":1"||e.what=="1"?"#ff0000":"#0000ff";
		context.fillRect(x,0,1,this.canvasHeight);
		break;
	    }
	for (const e of this.toneEvents)
	    if (t0>=e.t && t0<e.t+e.duration){
		context.fillStyle = this.keyContainsNoteColor(key,e.note)?"#ff00ff":"#ff00a0";
		let note=e.note;
		const bendIndex=binarySearch(e.bends,(x)=>x.t<=t0);
		if (bendIndex)
		    note += e.bends[bendIndex-1].bend*24/8191;
		const y0=Math.floor((settings.maxNote-note-.5)/(settings.maxNote-settings.minNote)*this.canvasHeight);
		const y1=Math.floor((settings.maxNote-note+.5)/(settings.maxNote-settings.minNote)*this.canvasHeight);
		context.fillRect(x,y0,1,y1-y0);
	    }
	for (const e of this.lyricEvents){
	    if (e.t>=lt0 && e.t<lt1){
		let k=e.what;
		if (k.slice(0,1)==">")
		    k = k.slice(1);
		if (k=="@" || k=="@@")
		    continue;
		if (k.slice(0,1)=="-")
		    k = k.slice(1);
		if (k.length!=0){
		    const y=Math.floor(e.note==undefined?this.canvasHeight/2:((settings.maxNote-e.note)/(settings.maxNote-settings.minNote)-.021)*this.canvasHeight);
		    for (let i=0; i<2; i++){
			const span=document.createElement("span");
			span.style.position = "absolute";
			span.style.left = (x-this.canvasWidth+i*this.canvasWidth)+"px";
			span.style.top = y+"px";
			span.innerHTML = k;
			div[i].appendChild(span);
		    }
		}
	    }
	}
    }
    drawFft(x,width,which){
	x += Math.floor(this.canvasWidth/2-settings.microphoneLatency*this.canvasWidth/settings.snakeTime);
	which += Math.floor(x/this.canvasWidth);
	x -= which*this.canvasWidth;
	const context=this.canvases[which%this.canvases.length].getContext("2d");
	context.globalCompositeOperation = "lighten";
	const i0=Math.floor(Math.exp((settings.minNote-69)/12*Math.log(2))*440*settings.fftSize/audioContext.sampleRate);
	const i1=Math.floor(Math.exp((settings.maxNote-69)/12*Math.log(2))*440*settings.fftSize/audioContext.sampleRate);
	for (let i=i0; i<i1; i++){
	    const n0=69+Math.log(i*audioContext.sampleRate/settings.fftSize/440)/Math.log(2)*12;
	    const n1=69+Math.log((i+1)*audioContext.sampleRate/settings.fftSize/440)/Math.log(2)*12;
	    const y0=Math.floor((settings.maxNote-n0)/(settings.maxNote-settings.minNote)*this.canvasHeight);
	    const y1=Math.floor((settings.maxNote-n1)/(settings.maxNote-settings.minNote)*this.canvasHeight);
	    let d=this.fft_data[i];
	    context.fillStyle = "rgb("+d+","+d+","+0+")";
	    context.fillRect(x,y0,width,y1-y0);
	}
    }
    fixScroll(){
	if (!this.canvasWidth)
	    return;
	const minScroll=Math.max(this.lastX-(this.canvases.length-2)*this.canvasWidth,2*this.canvasWidth);
	this.scroll = Math.max(Math.min(this.scroll,this.lastX),minScroll);
	this.lt.style.display = this.enabled&&this.scroll!=minScroll&&!isPlaying?"block":"none";
	this.gt.style.display = this.enabled&&this.scroll!=this.lastX&&!isPlaying?"block":"none";
	const time=Math.floor((this.scroll-2*this.canvasWidth)*settings.snakeTime/this.canvasWidth+this.canvasTime-startTime);
	this.drawLetters(time);
	beatCounter.setTime(time);
	const which=Math.floor(this.scroll/this.canvasWidth)-2;
	for (let i=0; i<this.canvases.length; i++){
	    this.canvases[(which+this.canvases.length+i)%this.canvases.length].style.display = i<2&&this.enabled?"block":"none";
	    this.divs[(which+this.canvases.length+i)%this.divs.length].style.display = i<2&&this.enabled?"block":"none";
	}
	for (let i=0; i<2; i++){
	    this.canvases[(which+this.canvases.length+i)%this.canvases.length].style.left = (which+2+i)*this.canvasWidth-this.scroll+"px";
	    this.divs[(which+this.canvases.length+i)%this.divs.length].style.left = (which+2+i)*this.canvasWidth-this.scroll+"px";
	}
    }
    animate(now){
	const rect=this.staticDiv.getBoundingClientRect();
	if (this.canvasWidth!=Math.ceil(rect.width) || this.canvasHeight!=Math.ceil(rect.height) || !this.canvasTime){
	    this.canvasWidth = Math.ceil(rect.width);
	    this.canvasHeight = Math.ceil(rect.height);
	    for (const canvas of this.canvases){
		canvas.width = this.canvasWidth;
		canvas.height = this.canvasHeight;
		canvas.style.width = this.canvasWidth+"px";
		canvas.style.height = this.canvasHeight+"px";
	    }
	    for (const div of this.divs){
		div.style.width = this.canvasWidth+"px";
		div.style.height = this.canvasHeight+"px";
	    }
	    this.canvasTime = now;
	    this.lastX = 0;
	    this.lastFftX = 0;
	}
	this.pointerDown = undefined;
	const maxX=Math.floor((now-this.canvasTime)*this.canvasWidth/settings.snakeTime)+2*this.canvasWidth;
	for (this.lastX=Math.max(this.lastX,maxX-2*this.canvasWidth); this.lastX<maxX; this.lastX++)
	    this.drawCanvas(this.lastX);
	if (this.fft!=undefined){
	    this.fft.getByteFrequencyData(this.fft_data);
	    this.drawFft(this.lastFftX,maxX-2*this.canvasWidth-this.lastFftX,0);
	    this.drawFft(this.lastFftX,maxX-2*this.canvasWidth-this.lastFftX,1);
	    this.lastFftX = maxX-2*this.canvasWidth;
	}
	this.scroll = this.lastX;
	this.fixScroll();
    }
}
