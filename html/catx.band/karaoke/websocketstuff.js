class WebsocketStuff{
    constructor(){
	this.chatQueue = [];
    }
    do_ntp(){
	if (--this.timeout_count<0)
	    this.start();
	else{
	    this.websocket.send("ntp"+new Date().getTime());
	    setTimeout(this.do_ntp.bind(this),250);
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
	    this.ntpArray[this.ntpCount] = .5*(new Date().getTime()+parseInt(a[0]))-parseInt(a[1]);
	    this.ntpCount = (this.ntpCount+1)%60;
	    this.timeout_count = 4;
	    while (this.chatQueue.length)
		this.sendChat(this.chatQueue.shift());
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
    sendChat(chat){
	this.websocket.send("chat"+chat);
    }
}
