ajaxError = function(xhr) {
  alert('Ajax 處理錯誤!');
}

mainChat = function (){
  $.ajaxSetup({ cache: false });
  $.ajax({
    url: 'main-chat',
    type: 'POST',
    data: {
      text: $('#main-chat-textarea').val(),
      type: $('#main-chat-select').val()
    },
    error: ajaxError,
    success: function(response) {
      $('#main-chat-table').html(response);
    }
  });
  $('#main-chat-textarea').val("");
};

var main_chat_timeout_var;
var game_list_table;
mainChatRefresh = function (){
  $.ajaxSetup({ cache: false });
  $.ajax({
    url: 'main-chat',
    type: 'GET',
    data: {},
    error: ajaxError,
    success: function(response) {
      $('#main-chat-table').html(response);
      main_chat_timeout_var=setTimeout("mainChatRefresh()",10000);
      $('#game-list-table').html(game_list_table);
      //gameListRefresh();
    }
  });
};

var game_list_timeout_var;
gameListRefresh = function (){
  $.ajaxSetup({ cache: false });
  $.ajax({
    url: 'game-list',
    type: 'GET',
    data: {},
    error: ajaxError,
    success: function(response) {
      $('#game-list-table').html(response);
      game_list_table = response;
      game_list_timeout_var=setTimeout("gameListRefresh()",15500);
    }
  });
};

createGame = function (){
  $.ajaxSetup({ cache: false });
  $.ajax({
    url: 'create-game',
    type: 'POST',
    data: {
      gamename: $('#gamename').val(),
      description: $('#description').val(),
      maxplayer: $('#maxplayer').val(),
      roundtime: $('#roundtime').val(),
      testmode: $('#testmode').val(),
      wishrole: $('#wishrole').val(),
      captcha: $('#captcha-input').val()
    },
    error: ajaxError,
    success: function(response) {
      var object = jQuery.parseJSON(response);

      if (object.length == 0) {
        $('#gamename').val(''),
        $('#description').val(''),
        $('#roundtime').val('180'),
        $('#captcha-input').val('')
        alert("村莊建立成功");
      } else {
        alert(object.join("\n"));
      }
      var img_src = $('#captcha').attr('src');
      var timestamp = new Date().getTime();
      $('#captcha').attr('src',img_src+'?'+timestamp);
    }
  });
};

$(document).ready(function() {

  $('#tabs').tabs({
    ajaxOptions: {
      error: function(xhr, status, index, anchor) {
        $(anchor.hash).html('頁面讀取失敗');
      }
    }
  });


  $("#login-button").click(function() {
    $('#menu-form').submit();
      return false;
  });

  $("#logout-button").click(function() {
    $('#menu-form').submit();
      return false;
  });

  $("#main-chat-button").click(function() {
    mainChat();
    return false;
  });

  $("#create-game-button").click(function() {
    createGame();
    return false;
  });
  
  $("#main-chat-textarea").keydown(function(moz_ev) {
    var ev = null;
    if (window.event)
      ev = window.event;
    else
      ev = moz_ev;
    
    if (ev != null && ev.ctrlKey && ev.keyCode == 13) 
      $("#main-chat-button").click();
  });

  mainChatRefresh();
  gameListRefresh();

  $('#reload-captcha').click(function() {
    var img_src = $('#captcha').attr('src');
    var timestamp = new Date().getTime();
    $('#captcha').attr('src',img_src+'?'+timestamp);
  });
});  

