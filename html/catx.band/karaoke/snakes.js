class Snakes{
    constructor(audioContext){
	this.audioContext = audioContext;
    }
    enable(where){
	this.canvasWidth = 1024;
	this.canvasHeight = (settings.maxNote-settings.minNote)*10;
	this.canvases = [];
	this.divs = [];
	for (let i=0; i<2; i++){
	    let canvas=document.createElement("canvas");
	    canvas.style = "position:absolute;top:20vh;left:0;width:100vw;height:80vh;background-color:#000;display:block;";
	    canvas.width = this.canvasWidth;
	    canvas.height = this.canvasHeight;
	    where.appendChild(canvas);
	    this.canvases[i] = canvas;
	    let div=document.createElement("div");
	    div.style = "position:absolute;top:20vh;left:0;width:100vw;height:80vh;font-size:1vw;white-space:nowrap;color:#0f0;display:block;z-index:1;";
	    where.appendChild(div);
	    this.divs[i] = div;
	}
	navigator.mediaDevices.getUserMedia({audio:{echoCancellation:{ideal:false}}}).then(stream=>{
	    const microphone=this.audioContext.createMediaStreamSource(stream);
	    this.fft = this.audioContext.createAnalyser();
	    this.fft.fftSize = settings.fftSize;
	    this.fft.minDecibels = -settings.microphoneSensitivity;
	    this.fft.maxDecibels = this.fft.minDecibels+20;
	    microphone.connect(this.fft);
	    this.fft_data = new Uint8Array(this.fft.frequencyBinCount);
	},err=>alert(err));
    }
    reset(startTime,repeat){
	this.startTime = startTime;
	this.canvasTime = startTime-settings.snakeTime/2;
	this.lastFftTime = 0;
	this.whichCanvas = 0;
	this.repeat = repeat;
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
    addKeysignatureEvent(event){
	this.keysignatureEvents.push(event);
    }
    keyContainsNote(key,note){
	note = (note+12-[11,6,1,8,3,10,5,0,7,2,9,4,11,6,1][key+7])%12;
	return note==0 || note==2 || note==4 || note==5 || note==7 || note==9 || note==11;
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
	    list.push({t:t,key0:this.keysignatureEvents[ki].key0});
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
		if (this.keyContainsNote(e.key0,note)){
		    const y=(settings.maxNote-note)/(settings.maxNote-settings.minNote)*100;
		    sb += "<div style=position:absolute;left:"+x0+"%;width:"+(x1-x0)+"%;top:"+y+"%;height:1px;background-color:#888;></div>"
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
	    if (this.repeat){
		const tt=Math.floor(t0/this.repeat)*this.repeat;
		t0 -= tt;
		t1 -= tt;
	    }
	    context.fillStyle = "#404040";
	    for (const e of this.beatEvents)
		if (e.t>=t0 && e.t<t1)
		    context.fillStyle = e.what.slice(-2)==":1"||e.what.slice(-2)=="=1"?"#ff4040":"#4040ff";
	    context.fillRect(x,0,1,this.canvasHeight);
	    context.fillStyle = "#ff40ff";
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
	context.fillStyle = "#404040";
	context.fillRect(x0,0,x1-x0,this.canvasHeight);
	context.globalCompositeOperation = "lighten";
	const i0=Math.floor(Math.exp((settings.minNote-69)/12*Math.log(2))*440*settings.fftSize/this.audioContext.sampleRate);
	const i1=Math.floor(Math.exp((settings.maxNote-69)/12*Math.log(2))*440*settings.fftSize/this.audioContext.sampleRate);
	for (let i=i0; i<i1; i++){
	    const n0=69+Math.log(i*this.audioContext.sampleRate/settings.fftSize/440)/Math.log(2)*12;
	    const n1=69+Math.log((i+1)*this.audioContext.sampleRate/settings.fftSize/440)/Math.log(2)*12;
	    const y0=Math.floor((settings.maxNote-n0)/(settings.maxNote-settings.minNote)*this.canvasHeight);
	    const y1=Math.floor((settings.maxNote-n1)/(settings.maxNote-settings.minNote)*this.canvasHeight);
	    let d=this.fft_data[i];
	    context.fillStyle = "rgb("+d+","+d+","+0+")";
	    context.fillRect(x0,y0,x1-x0,y1-y0);
	}
    }
    animate(now){
	if (this.canvases==undefined)
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
