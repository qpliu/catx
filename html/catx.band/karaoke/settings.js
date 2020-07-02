class Settings{
    constructor(where){
	this.settings = [
	    ["Delay","delay","200",Number,
		"Delay in milliseconds to synchronize sound with Jamulus.  Increase if the 球球 bounces too early."],
	    ["Microphone Latency","microphoneLatency","150",Number,
		"Microphone Latency in milliseconds.  Adjust this to line up times of yellow and purple snakes."],
	    ["Vocal Part","vocalPart","1",Number,
		"Vocal part.  Use 1 for first vocal part.  Use 2 for second vocal part.  You have to restart song after you change this."],
	    [null,"speaker","0",Number,null],
	    ["Snake Time","snakeTime","6000",Number,
		"Number of milliseconds of snakes to display."],
	    ["Lowest Note","minNote","48",Number,
		"Lowest note for snakes.  60 is middle C.  72 is C above middle C, etc.  If the snakes are disappearing off the bottom, make this number smaller."],
	    ["Highest Note","maxNote","84",Number,
		"Highest note for snakes.  60 is middle C.  72 is C above middle C, etc.  If the snakes are disappearing off the top, make this number bigger."],
	    ["Microphone Sensitivity","microphoneSensitivity","60",Number,
		"Increase this to make microphone more sensitive.  If you have to yell really loud, try making this bigger."],
	    ["fftSize","fftSize","8192",Number,
		"This must be power of 2 between 32 and 32768.  Bigger fftSize increases frequency resolution and decreases time resolution."],
	];
	for (const s of this.settings){
	    let match=document.cookie.match(s[1]+"=([^;]*)");
	    let value=match && match[1] || s[2];
	    this[s[1]] = s[3](value);
	    if (s[0]){
		const div=document.createElement("div");
		div.className = "tooltip";
		const label=document.createElement("label");
		label.innerHTML = s[0]+": ";
		div.appendChild(label);
		const input=document.createElement("input");
		input.type = "text";
		input.value = String(this[s[1]]);
		input.onchange = ()=>{
		    const value=s[3](input.value);
		    if (s[3]!=Number || !isNaN(value)){
			this[s[1]] = value;
			this.makeEverythingAgree();
		    }
		};
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
	this.makeEverythingAgree();
    }
    resetToDefault(){
	for (const s of this.settings)
	    if (s[0])
		this[s[1]] = s[3](s[2]);
	this.makeEverythingAgree();
    }
    makeEverythingAgree(){
	for (const s of this.settings){
	    document.cookie = s[1]+"="+this[s[1]]+";max-age=31536000;SameSite=Strict";
	    if (s.input)
		s.input.value = String(this[s[1]]);
	}
    }
}
