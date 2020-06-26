class Settings{
    constructor(){
	this.locationSearch = location.search;
	this.delay = Number(this.getSearch("delay","200"));
	this.timePerSnake = Number(this.getSearch("timePerSnake","4000"));
	this.minNote = Number(this.getSearch("lowestNote","48"));
	this.maxNote = Number(this.getSearch("highestNote","72"));
	this.minDecibels = Number(this.getSearch("minDecibels","-100"));
	this.maxDecibels = Number(this.getSearch("maxDecibels","-50"));
	this.transpose = Number(this.getSearch("transpose","0"));
	this.harmonics = Number(this.getSearch("harmonics","3"));
	this.subharmonics = Number(this.getSearch("subharmonics","3"));
	this.fftSize = Number(this.getSearch("fftSize","8192"));
	this.snakes = Number(this.getSearch("snakes","0"));
    }
    getSearch(what,defaul){
	let match=this.locationSearch.match("[?&]"+what+"=([^?&]*)");
	if (match && match[1])
	    return match[1];
	this.locationSearch += (this.locationSearch?"&":"?")+what+"="+defaul;
	return defaul;
    }
    make(where,label,what){
	const lab=document.createElement("label");
	lab.innerHTML = label;
	where.appendChild(lab);
	lab.for = what;
	const input=document.createElement("input");
	input.type = "text";
	input.name = what;
	let match=this.locationSearch.match("[?&]"+what+"=([^?&]+)");
	if (match)
	    input.value = match[1];
	where.appendChild(input);
	where.appendChild(document.createElement("br"));
    }
    makeInputs(where){
	this.make(where,"Snakes:","snakes");
	this.make(where,"Delay:","delay");
	this.make(where,"Transpose:","transpose");
	this.make(where,"Time Per Snake:","timePerSnake");
	this.make(where,"Lowest Note:","lowestNote");
	this.make(where,"Highest Note:","highestNote");
	this.make(where,"Harmonics:","harmonics");
	this.make(where,"Subharmonics:","subharmonics");
	this.make(where,"FFT Size:","fftSize");
	this.make(where,"minDecibels:","minDecibels");
	this.make(where,"maxDecibels:","maxDecibels");
    }
}
