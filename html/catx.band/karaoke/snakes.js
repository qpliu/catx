class Snakes{
    constructor(where){
	this.canvases = [];
	this.divs = [];
	for (let i=0; i<2; i++){
	    let canvas=document.createElement("canvas");
	    canvas.style = "position:absolute;top:20vh;left:0;width:100vw;height:80vh;background-color:#000;";
	    where.appendChild(canvas);
	    this.canvases[i] = canvas;
	    let div=document.createElement("div");
	    div.style = "position:absolute;top:20vh;left:0;width:100vw;height:80vh;font-size:1vw;white-space:nowrap;color:#0f0;z-index:1;";
	    where.appendChild(div);
	    this.divs[i] = div;
	}
	this.staticDiv = document.createElement("div");
	this.staticDiv.style = "position:absolute;top:19vh;left:0;width:100vw;height:80vh;font-size:2vh;color:#0ff;z-index:2;";
	where.appendChild(this.staticDiv);
    }
    setEnabled(enabled){
	this.enabled = enabled;
	this.canvasWidth = 1024;
	this.canvasHeight = (settings.maxNote-settings.minNote)*10;
	const display=enabled?"block":"none";
	for (let i=0; i<2; i++){
	    this.canvases[i].width = this.canvasWidth;
	    this.canvases[i].height = this.canvasHeight;
	    this.canvases[i].style.display = display;
	    this.divs[i].style.display = display;
	}
	this.staticDiv.style.display = display;
	if (enabled)
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
    reset(startTime,repeat,songLength){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.canvasTime = startTime-settings.snakeTime/2;
	this.lastFftTime = 0;
	this.whichCanvas = 0;
	this.toneEvents = [];
	this.lyricEvents = [];
	this.keysignatureEvents = [];
	this.beatEvents = [];
	this.canvasTime = 0;
	this.whichCanvas = 0;
    }
    addToneEvent(t,duration,note){
	this.toneEvents.push({t:t,duration:duration,note:note});
    }
    addBeatEvent(t,what){
	this.beatEvents.push({t:t,what:what});
    }
    addLyricEvent(t,what){
	this.lyricEvents.push({t:t,what:what});
    }
    addKeysignatureEvent(e){
	this.keysignatureEvents.push({t:e.t,key:[e.key0,e.key1]});
    }
    noteToString(key,note){
	return [["C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B"],["C","D♭","D","E♭","E","F","G♭","G","A♭","A","B♭","B"]][key[0]<0?1:0][note%12];
    }
    keyContainsNote(key,note){
	note = (note+12-[11,6,1,8,3,10,5,0,7,2,9,4,11,6,1][key[0]+7])%12;
	return note==0 || note==2 || note==4 || note==5 || note==7 || note==9 || note==11;
    }
    drawLetters(time){
	let bestTime=Infinity;
	let bestKey=[0,0];
	for (const e of this.keysignatureEvents){
	    let t=time;
	    if (this.repeat && t>=this.songLength)
		t -= Math.floor((t-this.songLength)/this.repeat+1)*this.repeat;
	    t -= e.t;
	    if (t>0 && t<bestTime){
		bestTime = t;
		bestKey = e.key;
	    }
	}
	let sb="";
	for (let note=settings.minNote+1; note<settings.maxNote; note++)
	    if (this.keyContainsNote(bestKey,note)){
		const y=(settings.maxNote-note)/(settings.maxNote-settings.minNote)*100;
		sb += "<span style=position:absolute;left:50%;top:"+y+"%;>"+this.noteToString(bestKey,note)+"</span>"
	    }
	this.staticDiv.innerHTML = sb;
    }
    drawDiv(div,time){
	let sb="";
	const list=[];
	let r=this.repeat?Math.floor(time/this.repeat)*this.repeat:0;
	for (let ki=0; ki<this.keysignatureEvents.length;){
	    const t=r+this.keysignatureEvents[ki].t;
	    if (t>=time+settings.snakeTime)
		break;
	    if (list.length!=0)
		list[list.length-1].end = t;
	    list.push({t:t,key:this.keysignatureEvents[ki].key});
	    if (ki==this.keysignatureEvents.length-1 && this.repeat){
		r += this.repeat;
		ki = 0;
	    }else
		ki++;
	}
	if (list.length!=0)
	    list[list.length-1].end = time+settings.snakeTime;
	for (const e of list){
	    const x0=(e.t-time)*100/settings.snakeTime;
	    const x1=(e.end-time)*100/settings.snakeTime;
	    for (let note=settings.minNote+1; note<settings.maxNote; note++)
		if (this.keyContainsNote(e.key,note)){
		    const y=(settings.maxNote-note)/(settings.maxNote-settings.minNote)*100;
		    sb += "<div style=position:absolute;left:"+x0+"%;width:"+(x1-x0)+"%;top:"+y+"%;height:1px;background-color:#444;></div>"
		}
	}
	for (const e of this.lyricEvents){
	    const r0=this.repeat?Math.ceil((time-settings.snakeTime-e.t)/this.repeat):0;
	    const r1=this.repeat?Math.ceil((time+settings.snakeTime-e.t)/this.repeat):1;
	    for (let r=r0; r<r1; r++){
		const t=e.t+this.repeat*r;
		if (t>time-settings.snakeTime && t<time+settings.snakeTime){
		    let k=e.what;
		    if (k.slice(0,1)==">")
			k = k.slice(1);
		    if (k=="@" || k=="@@")
			continue;
		    if (k.slice(0,1)=="-")
			k = k.slice(1);
		    if (k.length!=0)
			sb += "<span style=position:absolute;left:"+(t-time)*100/settings.snakeTime+"vw;bottom:0>"+k+"</span>"
		}
	    }
	}
	div.innerHTML = sb;
    }
    drawCanvas(canvas,time){
	const context=canvas.getContext("2d");
	context.globalCompositeOperation = "source-over";
	for (let x=0; x<this.canvasWidth; x++){
	    let t0=time+x*settings.snakeTime/this.canvasWidth;
	    let t1=time+(x+1)*settings.snakeTime/this.canvasWidth;
	    if (this.repeat && t0>=this.songLength){
		const tt=Math.floor((t0-this.songLength)/this.repeat+1)*this.repeat;
		t0 -= tt;
		t1 -= tt;
	    }
	    context.fillStyle = "#303030";
	    for (const e of this.beatEvents)
		if (e.t>=t0 && e.t<t1)
		    context.fillStyle = e.what.slice(-2)==":1"||e.what.slice(-2)=="=1"?"#ff3030":"#3030ff";
	    context.fillRect(x,0,1,this.canvasHeight);
	    context.fillStyle = "#ff30ff";
	    for (const e of this.toneEvents)
		if (t0>=e.t && t0<e.t+e.duration){
		    const y0=(settings.maxNote-e.note-.5)/(settings.maxNote-settings.minNote)*this.canvasHeight;
		    const y1=(settings.maxNote-e.note+.5)/(settings.maxNote-settings.minNote)*this.canvasHeight;
		    context.fillRect(x,y0,1,y1-y0);
		}
	}
    }
    drawCanvasAndDiv(which,time){
	this.drawDiv(this.divs[which],time-this.startTime-settings.snakeTime/2);
	this.drawCanvas(this.canvases[which],time-this.startTime-settings.snakeTime/2);
    }
    drawFft(canvas,time,now){
	const context=canvas.getContext("2d");
	const x0=Math.floor((this.lastFftTime-time)*this.canvasWidth/settings.snakeTime)+this.canvasWidth/2;
	const x1=Math.floor((now-time)*this.canvasWidth/settings.snakeTime)+this.canvasWidth/2;
	context.globalCompositeOperation = "difference";
	context.fillStyle = "#303030";
	context.fillRect(x0,0,x1-x0,this.canvasHeight);
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
	    context.fillRect(x0,y0,x1-x0,y1-y0);
	}
    }
    animate(now){
	if (!this.enabled)
	    return;
	if (this.canvasTime<now-2*settings.snakeTime){
	    this.canvasTime = now-settings.snakeTime;
	    this.drawCanvasAndDiv(this.whichCanvas^1,now);
	}
	if (now>=this.canvasTime+settings.snakeTime){
	    this.canvasTime += settings.snakeTime;
	    this.drawCanvasAndDiv(this.whichCanvas,this.canvasTime+settings.snakeTime);
	    this.whichCanvas ^= 1;
	}
	this.drawLetters(now-this.startTime);
	if (this.fft!=undefined){
	    this.fft.getByteFrequencyData(this.fft_data);
	    this.drawFft(this.canvases[this.whichCanvas],this.canvasTime,now);
	    this.drawFft(this.canvases[this.whichCanvas^1],this.canvasTime+settings.snakeTime,now);
	    this.lastFftTime = now;
	}
	this.canvases[this.whichCanvas].style.left = (this.canvasTime-now)*100/settings.snakeTime+"%";
	this.canvases[this.whichCanvas^1].style.left = 100+(this.canvasTime-now)*100/settings.snakeTime+"%";
	this.divs[this.whichCanvas].style.left = (this.canvasTime-now)*100/settings.snakeTime+"%";
	this.divs[this.whichCanvas^1].style.left = 100+(this.canvasTime-now)*100/settings.snakeTime+"%";
    }
}
