$(document).ready(function(){
  $(".number-input").keydown(function(e) {
    if (e.keyCode < 48 || e.keyCode > 57)
      e.preventDefault();
  });
});
