class WebsocketStuff{
    constructor(){
	this.chatQueue = [];
	this.ntpArray = [];
	this.ntpCount = 0;
    }
    do_ntp(){
	if (--this.timeout_count<0)
	    this.start();
	else{
	    this.ntp_t0 = new Date().getTime();
	    this.websocket.send("ntp");
	    setTimeout(this.do_ntp.bind(this),1000);
	}
    }
    onopen(){
	this.timeout_count = 4;
	this.do_ntp();
    }
    onmessage(event){
	if (event.data.slice(0,1)=="t"){
	    if (this.ntp_t0!=undefined){
		if (this.ntpArray.length==0)
		    this.websocket.send("middle");
		this.ntpArray[this.ntpCount] = (new Date().getTime()+this.ntp_t0)/2-parseInt(event.data.slice(1));
		this.ntpCount = (this.ntpCount+1)%60;
	    }
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
	this.ntp_t0 = undefined;
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
