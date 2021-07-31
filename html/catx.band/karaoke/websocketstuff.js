class WebsocketStuff{
    constructor(){
	this.chatQueue = [];
    }
    do_ntp(){
	if (++this.timeout_count==5)
	    this.start();
	else{
	    this.websocket.send("ntp"+new Date().getTime());
	    setTimeout(this.do_ntp.bind(this),1000);
	}
    }
    onopen(){
	this.timeout_count = 0;
	this.do_ntp();
    }
    onmessage(event){
	if (event.data.slice(0,1)=="t"){
	    const a=event.data.slice(1).split(",");
	    const t0=parseInt(a[0]);
	    const t1=new Date().getTime();
	    if (t1-t0>150 && ++this.ntpBadCount==4)
	        alert("Your internet connection is too slow to synchronize clocks.");
	    else if (t1-t0<this.ntpT1MinusT0){
		this.ntpT1MinusT0 = t1-t0;
		if (this.ntpOffset==undefined)
		    this.websocket.send("middle");
		this.ntpOffset = .5*(t0+t1)-parseInt(a[1]);
		this.ntpBadCount = Infinity;
	    }
	    this.timeout_count = 0;
	    while (this.chatQueue.length)
		this.websocket.send("chat"+this.chatQueue.shift());
	}
    }
    getNtpOffset(){
	return this.ntpOffset;
    }
    start(){
	this.ntpT1MinusT0 = Infinity;
	this.ntpBadCount = 0;
	this.ntpOffset = undefined;
	this.websocket = new WebSocket("wss://catx.band/karaoke_websocket");
	this.websocket.onopen = this.onopen.bind(this);
	this.websocket.onmessage = this.onmessage.bind(this);
    }
    chat(chat){
	this.chatQueue.push(chat);
    }
}
