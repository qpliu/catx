class Snakes{
    constructor(where){
	this.canvases = [];
	for (let i=0; i<2; i++){
	    this.canvases[i] = document.createElement("canvas");
	    this.canvases[i].style = "position:absolute;top:20vh;left:0;width:100vw;height:80vh;background-color:#000;";
	    where.appendChild(this.canvases[i] );
	}
	this.staticDiv = document.createElement("div");
	this.staticDiv.style = "position:absolute;top:20vh;left:0;width:100vw;height:80vh;font-size:2vh;color:#0ff;z-index:2;";
	where.appendChild(this.staticDiv);
    }
    reset(startTime,repeat,songLength){
	this.startTime = startTime;
	this.repeat = repeat;
	this.songLength = songLength;
	this.lastFftTime = 0;
	this.toneEvents = [];
	this.lyricEvents = [];
	this.keysignatureEvents = [];
	this.beatEvents = [];
	this.canvasTime = 0;
	this.whichCanvas = 0;
    }
    setEnabled(enabled){
	this.enabled = enabled;
	this.canvasTime = 0;
	this.canvasWidth = 1024;
	this.canvasHeight = (settings.maxNote-settings.minNote)*10;
	const display=enabled?"block":"none";
	for (let i=0; i<2; i++){
	    this.canvases[i].width = this.canvasWidth;
	    this.canvases[i].height = this.canvasHeight;
	    this.canvases[i].style.display = display;
	}
	this.staticDiv.style.display = display;
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
	return [["C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B"],["C","D♭","D","E♭","E","F","G♭","G","A♭","A","B♭","B"]][key[0]<0?1:0][note%12];
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
	    if (t>0 && t<bestTime){
		bestTime = t;
		bestKey = e.key;
	    }
	}
	let sb="";
	for (let note=settings.minNote+1; note<settings.maxNote; note++)
	    if (this.keyContainsNoteColor(bestKey,note)){
		const y=(settings.maxNote-note)/(settings.maxNote-settings.minNote)*80-1.1;
		sb += "<span style=position:absolute;left:50%;top:"+y+"vh;>"+this.noteToString(bestKey,note)+"</span>"
	    }
	if (this.staticDiv.innerHTML!=sb)
	    this.staticDiv.innerHTML = sb;
    }
    drawCanvas(which,time){
	time -= this.startTime+settings.snakeTime/2;
	const context=this.canvases[which].getContext("2d");
	context.globalCompositeOperation = "source-over";
	for (let x=0; x<this.canvasWidth; x++){
	    let t0=time+x*settings.snakeTime/this.canvasWidth;
	    let t1=time+(x+1)*settings.snakeTime/this.canvasWidth;
	    if (this.repeat && t0>=this.songLength){
		const tt=Math.ceil((t1-this.songLength)/this.repeat)*this.repeat;
		t0 -= tt;
		t1 -= tt;
	    }
	    let key=[0,0];
	    for (const e of this.keysignatureEvents){
		if (e.t>t0)
		    break;
		key = e.key;
	    }
	    context.fillStyle = "#000000";
	    for (const e of this.beatEvents)
		if (e.t>=t0 && e.t<t1)
		    context.fillStyle = e.what.slice(-2)==":1"||e.what=="1"?"#ff0000":"#0000ff";
	    context.fillRect(x,0,1,this.canvasHeight);
	    context.fillStyle = "#ff00ff";
	    for (const e of this.toneEvents)
		if (t0>=e.t && t0<e.t+e.duration){
		    const y0=(settings.maxNote-e.note-.5)/(settings.maxNote-settings.minNote)*this.canvasHeight;
		    const y1=(settings.maxNote-e.note+.5)/(settings.maxNote-settings.minNote)*this.canvasHeight;
		    context.fillRect(x,y0,1,y1-y0);
		}
	    for (let note=settings.minNote+1; note<settings.maxNote; note++){
		const color=this.keyContainsNoteColor(key,note);
		if (color!=undefined){
		    context.fillStyle = color;
		    const y=(settings.maxNote-note)/(settings.maxNote-settings.minNote)*this.canvasHeight;
		    context.fillRect(x,y,1,1);
		}
	    }
	}
	context.fillStyle = "#00ff00";
	context.font = "bold 16px serif";
	for (const e of this.lyricEvents){
	    const r0=this.repeat?Math.max(Math.ceil((time-settings.snakeTime-e.t)/this.repeat),0):0;
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
		    if (k.length!=0){
			const y=e.note==undefined?0:(settings.maxNote-e.note)/(settings.maxNote-settings.minNote)*this.canvasHeight+5;
			context.fillText(k,(t-time)*this.canvasWidth/settings.snakeTime,y);
		    }
		}
	    }
	}
    }
    drawFft(canvas,time,now){
	const context=canvas.getContext("2d");
	const xx0=Math.floor((this.lastFftTime-time)*this.canvasWidth/settings.snakeTime)+this.canvasWidth/2;
	const xx1=Math.floor((now-time)*this.canvasWidth/settings.snakeTime)+this.canvasWidth/2;
	const x0=Math.floor((this.lastFftTime-settings.microphoneLatency-time)*this.canvasWidth/settings.snakeTime)+this.canvasWidth/2;
	const x1=Math.floor((now-settings.microphoneLatency-time)*this.canvasWidth/settings.snakeTime)+this.canvasWidth/2;
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
	    this.drawCanvas(this.whichCanvas^1,now);
	    this.lastFftTime = now;
	}
	if (now>=this.canvasTime+settings.snakeTime){
	    this.canvasTime += settings.snakeTime;
	    this.drawCanvas(this.whichCanvas,this.canvasTime+settings.snakeTime);
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
    }
}
