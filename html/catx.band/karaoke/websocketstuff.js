class WebsocketStuff{
    constructor(){
	this.chatQueue = [];
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
		if (this.ntp_offset==undefined)
		    this.websocket.send("middle");
		this.ntp_offset = (new Date().getTime()+this.ntp_t0)/2-parseInt(event.data.slice(1));
	    }
	    this.timeout_count = 4;
	    while (this.chatQueue.length)
		this.sendChat(this.chatQueue.shift());
	}
    }
    start(){
	this.ntp_t0 = undefined;
	this.websocket = new WebSocket("wss://catx.band/karaoke_websocket");
	this.websocket.onopen = this.onopen.bind(this);
	this.websocket.onmessage = this.onmessage.bind(this);
    }
    sendChat(chat){
	this.websocket.send("chat"+chat);
    }
}
