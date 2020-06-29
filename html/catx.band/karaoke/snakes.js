class Snakes{
    constructor(audioContext){
	this.audioContext = audioContext;
	this.canvasWidth = 1024;
	this.canvasHeight = (settings.maxNote-settings.minNote)*10;
    }
    enable(where){
	this.snakes = [];
	this.snakesw = [];
	for (let i=0; i<2; i++){
	    const canvas=document.createElement("canvas");
	    canvas.style = "position:absolute;top:"+(60-i*40)+"vh;left:0;width:100vw;height:35vh;background-color:#000;display:block;";
	    canvas.width = this.canvasWidth;
	    canvas.height = this.canvasHeight;
	    this.snakes.push(canvas);
	    where.appendChild(canvas);
	    const div=document.createElement("div");
	    div.style = "position:absolute;top:"+(95-i*40)+"vh;left:0;width:100vw;height:5vh;font-size:1vw;white-space:nowrap;color:#0f0;background-color:#000;display:block;z-index:1;";
	    this.snakesw.push(div);
	    where.appendChild(div);
	}
	navigator.mediaDevices.getUserMedia({audio:true}).then(stream=>{
	    const microphone=this.audioContext.createMediaStreamSource(stream);
	    this.fft = this.audioContext.createAnalyser();
	    this.fft.smoothingTimeConstant = 0;
	    this.fft.fftSize = settings.fftSize;
	    this.fft.minDecibels = -settings.microphoneSensitivity;
	    this.fft.maxDecibels = this.fft.minDecibels+20;
	    microphone.connect(this.fft);
	    this.fft_data = new Uint8Array(this.fft.frequencyBinCount);
	},err=>alert(err));
    }
    reset(startTime,repeat){
	this.startTime = startTime;
	this.lastFftTime = startTime;
	this.repeat = repeat;
	this.toneEvents = [];
	this.lyricEvents = [];
	this.beatEvents = [];
	this.snakeTime = 0;
	this.whichLine = 0;
	this.nextLineStart = 0;
    }
    addToneEvent(time,duration,note){
	this.toneEvents.push({time:time,duration:duration,note:note});
    }
    addBeatEvent(time,what){
	this.beatEvents.push({time:time,what:what});
    }
    addLyricEvent(time,what){
	this.lyricEvents.push({time:time,what:what});
    }
    animate(now){
	if (this.snakes==undefined)
	    return;
	if (this.fft!=undefined){
	    let canvas;
	    let canvas_start;
	    if (now<this.startTime+this.snakeTime-settings.timePerSnake){
		canvas = this.snakes[this.whichLine^1];
		canvas_start = this.startTime+this.snakeTime-2*settings.timePerSnake;
	    }else{
		canvas = this.snakes[this.whichLine];
		canvas_start = this.startTime+this.snakeTime-settings.timePerSnake;
	    }
	    const context=canvas.getContext("2d");
	    context.globalCompositeOperation = "lighter";
	    const x0=Math.floor(Math.max((this.lastFftTime-canvas_start)*this.canvasWidth/settings.timePerSnake,0));
	    const x1=Math.floor((now-canvas_start)*this.canvasWidth/settings.timePerSnake);
	    this.lastFftTime = now;
	    context.globalCompositeOperation = "difference";
	    context.fillStyle = "#404040";
	    context.fillRect(x0,0,x1-x0,this.canvasHeight);
	    context.globalCompositeOperation = "lighten";
	    this.fft.getByteFrequencyData(this.fft_data);
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
	while (this.nextLineStart<now){
	    this.snakeTime += Math.max(Math.floor((now-this.startTime-this.snakeTime)/(settings.timePerSnake*2)),0)*settings.timePerSnake*2;
	    this.whichLine ^= 1;
	    let sb=""
	    const canvas=this.snakes[this.whichLine];
	    const context=canvas.getContext("2d");
	    context.globalCompositeOperation = "source-over";
	    context.fillStyle = "#404040";
	    context.fillRect(0,0,this.canvasWidth,this.canvasHeight);
	    for (const e of this.lyricEvents){
		const r0=this.repeat?Math.ceil((this.snakeTime-settings.timePerSnake-e.time)/this.repeat):0;
		const r1=this.repeat?Math.ceil((this.snakeTime+settings.timePerSnake-e.time)/this.repeat):1;
		for (let r=r0; r<r1; r++){
		    const t=e.time+this.repeat*r;
		    if (t>this.snakeTime-settings.timePerSnake && t<this.snakeTime+settings.timePerSnake){
			let k=e.what;
			if (k.slice(0,1)==">")
			    k = k.slice(1);
			if (k=="@" || k=="@@")
			    continue;
			if (k.slice(0,1)=="-")
			    k = k.slice(1);
			if (k.length!=0)
			    sb += "<span style=position:absolute;left:"+(t-this.snakeTime)*100/settings.timePerSnake+"vw;bottom:0>"+k+"</span>"
		    }
		}
	    }
	    for (const e of this.beatEvents){
		const r0=this.repeat?Math.ceil((this.snakeTime-settings.timePerSnake-e.time)/this.repeat):0;
		const r1=this.repeat?Math.ceil((this.snakeTime+settings.timePerSnake-e.time)/this.repeat):1;
		for (let r=r0; r<r1; r++){
		    const t=e.time+this.repeat*r;
		    if (t>this.snakeTime-settings.timePerSnake && t<this.snakeTime+settings.timePerSnake){
			let k=e.what;
			if (k.slice(-2)==':1' || k.slice(-2)=='=1')
			    context.fillStyle = "#ff4040";
			else
			    context.fillStyle = "#4040ff";
			const x0=(t-this.snakeTime)/settings.timePerSnake*this.canvasWidth;
			context.fillRect(x0,0,1,this.canvasHeight);
		    }
		}
	    }
	    context.fillStyle = "#ff40ff";
	    for (const e of this.toneEvents){
		const y0=(settings.maxNote-e.note-.5)/(settings.maxNote-settings.minNote)*this.canvasHeight;
		const y1=(settings.maxNote-e.note+.5)/(settings.maxNote-settings.minNote)*this.canvasHeight;
		const r0=this.repeat?Math.ceil((this.snakeTime-(e.time+e.duration))/this.repeat):0;
		const r1=this.repeat?Math.ceil((this.snakeTime-e.time)/this.repeat):0;
		for (let r=r0; r<=r1; r++){
		    const x0=Math.max((e.time+this.repeat*r-this.snakeTime)/settings.timePerSnake,0)*this.canvasWidth;
		    const x1=Math.min(((e.time+e.duration)+this.repeat*r-this.snakeTime)/settings.timePerSnake,1)*this.canvasWidth;
		    if (x1>x0)
			context.fillRect(x0,y0,x1-x0,y1-y0);
		}
	    }
	    this.snakesw[this.whichLine].innerHTML = sb;
	    this.nextLineStart = this.startTime+this.snakeTime+settings.timePerSnake/2;
	    this.snakeTime += settings.timePerSnake;
	}
    }
}
