class WebsocketStuff{
    constructor(){
	this.chatQueue = [];
	this.badCount = 0;
    }
    do_ntp(){
	if (--this.timeout_count<0)
	    this.start();
	else{
	    this.websocket.send("ntp"+new Date().getTime());
	    setTimeout(this.do_ntp.bind(this),1000);
	}
    }
    onopen(){
	this.timeout_count = 4;
	this.do_ntp();
    }
    onmessage(event){
	if (event.data.slice(0,1)=="t"){
	    if (this.ntpArray.length==4)
		this.websocket.send("middle");
	    const a=event.data.slice(1).split(",");
	    const t0=parseInt(a[0]);
	    const t1=new Date().getTime();
	    if (Math.abs(t1-t0)<150){
		this.ntpArray[this.ntpCount] = .5*(t0+t1)-parseInt(a[1]);
		this.ntpCount = (this.ntpCount+1)%60;
		this.badCount = 0;
	    }else if (++this.badCount==4)
	        alert("Your internet connection is too slow to synchronize clocks.");
	    this.timeout_count = 4;
	    while (this.chatQueue.length)
		this.websocket.send("chat"+this.chatQueue.shift());
	}
    }
    getNtpOffset(){
	this.ntpArray.sort();
	return this.ntpArray[this.ntpArray.length>>1];
    }
    start(){
	this.ntpArray = [];
	this.ntpCount = 0;
	this.websocket = new WebSocket("wss://catx.band/karaoke_websocket");
	this.websocket.onopen = this.onopen.bind(this);
	this.websocket.onmessage = this.onmessage.bind(this);
    }
    chat(chat){
	this.chatQueue.push(chat);
    }
}
