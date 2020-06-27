class Settings{
    constructor(settings_a){
	this.settings_a = settings_a;
	this.settings = [
	    ["Snakes","snakes","0",Number],
	    ["Delay","delay","200",Number],
	    ["Speaker","speaker","0",Number],
	    ["Time Per Snake","timePerSnake","4000",Number],
	    ["Lowest Note","minNote","48",Number],
	    ["Highest Note","maxNote","72",Number],
	    ["minDecibels","minDecibels","-100",Number],
	    ["maxDecibels","maxDecibels","-50",Number],
	    ["Transpose","transpose","0",Number],
	    ["Harmonics","harmonics","3",Number],
	    ["Subharmonics","subharmonics","3",Number],
	    ["fftSize","fftSize","8192",Number],
	];
	for (const s of this.settings){
	    let match=location.search.match("[?&]"+s[1]+"=([^?&]*)");
	    let value=match && match[1] || s[2];
	    this[s[1]] = s[3](value);
	}
	this.setHref();
    }
    setHref(){
	let sb="setup.html";
	let sep="?";
	for (const s of this.settings){
	    sb += sep+s[1]+"="+this[s[1]];
	    sep = "&";
	}
	if (this.settings_a!=undefined)
	    this.settings_a.href = sb;
    }
    makeInputs(where){
	for (const s of this.settings){
	    const label=document.createElement("label");
	    label.innerHTML = s[0]+": ";
	    where.appendChild(label);
	    label.for = s[1];
	    const input=document.createElement("input");
	    input.type = "text";
	    input.name = s[1];
	    input.value = String(this[s[1]]);
	    where.appendChild(input);
	    where.appendChild(document.createElement("br"));
	}
    }
}
