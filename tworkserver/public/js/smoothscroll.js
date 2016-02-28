var scroll = true;
function startScroll() {
    	if (scroll == true) {
        window.scrollBy(0,20); // horizontal and vertical scroll increments
    	  scrolldelay = setTimeout('pageScroll()',100); // scrolls every 100 milliseconds
      }
}

function stopScroll() {
  scroll = false;
}
