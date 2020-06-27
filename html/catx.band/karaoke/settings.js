class Settings{
    constructor(settings_a){
	this.settings_a = settings_a;
	this.settings = [
	    ["Snakes","snakes","0",Number,
		"Use 0 for 球球, 1 for snakes."],
	    ["Delay","delay","200",Number,
		"Delay in milliseconds to synchronize sound with Jamulus.  Increase if the 球球 bounces too early."],
	    ["Speaker","speaker","0",Number,
		"0 turns off sound.  1 turns on sound."],
	    ["Time Per Snake","timePerSnake","4000",Number,
		"Number of milliseconds for each row of snakes."],
	    ["Lowest Note","minNote","48",Number,
		"Lowest note for snakes.  60 is middle C.  72 is C above middle C, etc.  If the snakes are disappearing off the bottom, make this number smaller."],
	    ["Highest Note","maxNote","72",Number,
		"Highest note for snakes.  60 is middle C.  72 is C above middle C, etc.  If the snakes are disappearing off the top, make this number bigger."],
	    ["minDecibels","minDecibels","-100",Number,
		"This somehow influences microphone sensitivity.  I don't know how.  Probably just leave it."],
	    ["maxDecibels","maxDecibels","-60",Number,
		"Make this more negative to increase microphone sensitivity.  If you have to yell really loud, try making this more negative.  Like -80."],
	    ["Transpose","transpose","0",Number,
		"Number of half steps to transpose the music up.  If you can't sing the high notes, try setting this to -12."],
	    ["Harmonics","harmonics","3",Number,
		"We try to cancel overtones by checking harmonics and subharmonics.  This is number of harmonics we check.  Try changing this to 1 or 2 if you have trouble getting yellow snake to appear."],
	    ["Subharmonics","subharmonics","3",Number,
		"We try to cancel overtones by checking harmonics and subharmonics.  This is number of subharmonics we check.  Try changing this to 1 or 2 if you have trouble getting yellow snake to appear."],
	    ["fftSize","fftSize","8192",Number,
		"This must be power of 2 between 32 and 32768.  Bigger fftSize increases frequency resolution and decreases time resolution."],
	];
	for (const s of this.settings){
	    let match=location.search.match("[?&]"+s[1]+"=([^?&]*)");
	    let value=match && match[1] || s[2];
	    this[s[1]] = s[3](value);
	}
	this.makeEverythingAgree();
    }
    resetToDefault(){
	for (const s of this.settings)
	    this[s[1]] = s[3](s[2]);
	this.makeEverythingAgree();
    }
    makeEverythingAgree(){
	let sb="setup.html";
	let sep="?";
	for (const s of this.settings){
	    sb += sep+s[1]+"="+this[s[1]];
	    sep = "&";
	    if ("input" in s)
		s.input.value = String(this[s[1]]);
	}
	if (this.settings_a!=undefined)
	    this.settings_a.href = sb;
    }
    makeInputs(where){
	for (const s of this.settings){
	    const div=document.createElement("div");
	    div.className = "tooltip";
	    const label=document.createElement("label");
	    label.innerHTML = s[0]+": ";
	    div.appendChild(label);
	    label.for = s[1];
	    const input=document.createElement("input");
	    input.type = "text";
	    input.name = s[1];
	    input.value = String(this[s[1]]);
	    div.appendChild(input);
	    const tooltiptext=document.createElement("span");
	    tooltiptext.innerHTML = s[4];
	    tooltiptext.className = "tooltiptext";
	    div.appendChild(tooltiptext);
	    where.appendChild(div);
	    where.appendChild(document.createElement("br"));
	    s.input = input;
	}
    }
}
