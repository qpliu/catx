class WebsocketStuff{
    do_ntp(){
	if (--this.timeout_count<0)
	    this.start();
	else{
	    this.ntp_t0 = new Date().getTime();
	    this.websocket.send("ntp");
	    const thiz=this;
	    setTimeout(function(){thiz.do_ntp()},1000);
	}
    }
    onopen(){
	this.timeout_count = 4;
	this.do_ntp();
    }
    onmessage(event){
	if (event.data.slice(0,1)=="t"){
	    if (this.ntp_t0!=undefined)
		this.ntp_offset = (new Date().getTime()+this.ntp_t0)/2-parseInt(event.data.slice(1));
	    this.timeout_count = 4;
	    if (this.ntp_offset==undefined)
		this.websocket.send("middle");
	}
    }
    start(){
	this.ntp_t0 = undefined;
	this.websocket = new WebSocket("wss://catx.band/karaoke_websocket");
	const thiz=this;
	this.websocket.onopen = function(){thiz.onopen();};
	this.websocket.onmessage = function(event){thiz.onmessage(event);};
    }
    sendChat(chat){
	this.websocket.send("chat"+chat);
    }
}
