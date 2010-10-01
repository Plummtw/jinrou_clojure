$(document).ready(function() {
  $('#copy-email2msn').click(function() {
    $('#msn').val($('#email').val());
  });

  $('#reload-captcha').click(function() {
    var img_src = $('#captcha').attr('src');
    var timestamp = new Date().getTime();
    $('#captcha').attr('src',img_src+'?'+timestamp);
  });

  var usericonHref = $("#usericon a");
  usericonHref.click(function() {
    var offset = $(this).attr("offset");
    alert(to);    // do something , ex: window.location.href=to});
  });
});

