/*
* moaModal
* MIT Licensed
* @author Mamod Mehyar
* http://twitter.com/mamod
* http://mamod.me
* version : 1.1.0
*/

(function($){
    "use strict";  
    var _SW_overlay
      , _modalWrapper
      , _SW_modal;
    
    //global options
    var modal_options =  {
        speed : 500,
        easing : 'linear',
        position : '0 auto',
        animation: 'none',
        on : 'click',
        escapeClose : true,
        overlayColor : '#000000',
        overlayOpacity : 0.7,
        overlayClose : true
    };
    
    //initiate overlay
    //and modal containers
    $(document).ready(function(){
        //overlay div
        _SW_overlay = $('<div></div>').css({
            width : '100%',
            height: $(document).height(),
            position : 'fixed',
            backgroundColor : '#000',
            overflow : 'hidden',
            opacity : 0.7,
            top : 0,
            left: 0,
            display : 'none',
            zIndex : '999996'
        }).appendTo('body');
        
        //modal wrapper
        _modalWrapper = $('<div></div>').css({
            width : '100%',
            height: $(document).height(),
            position : 'absolute',
            display: 'none',
            overflow : 'hidden',
            top : 0,
            left: 0,
            zIndex : '999998'
        }).appendTo('body');
        
        //modal container
        _SW_modal = $('<div></div>').css({
            position : 'relative',
            width : '100%',
            zIndex : '999997',
            top : 0,
            left : 0,
            visibility : 'hidden'
        }).appendTo(_modalWrapper);
    });
    
    var _modal = function (ele,obj) {
        obj = $.extend({},modal_options, obj);
        var target = $(obj.modal);
        if (!target.length){
            return;
        }
        
        obj.position = obj.position.replace( /(\d+)(\s|$)/g, "$1px" + "$2");
        
        var clone   = _SW_modal
          , expand  = obj.position.split(' ')
          , eTop    = expand[0]
          , eLeft   = expand[1];
        
        var css = {
            position: 'relative',
            overflow : 'hidden',
            display : 'block',
            zIndex : '999999',
            margin : obj.position
        };
        
        //fake screen resolution
        var cc = target.clone()
          , origWidth
          , origHeight;
        
        cc.appendTo('body').css({
            maxWidth : window.screen.width,
            maxHeight : window.screen.height
        });
        
        origWidth = cc.outerWidth();
        origHeight = cc.outerHeight();
        
        if (origHeight == 0){
            origHeight = '1%';
        }
        
        cc.remove();
        
        var from    = {}
          , fromArr = obj.animation.split(' ');
        
        for (var x = 0; x < fromArr.length; x++){
            from[fromArr[x]] = true;
        }
        
        if (obj.overlayClose !== false){
            _modalWrapper.click(function(e){
                if (this == e.target || e.target == clone[0]){
                    target.trigger('close.modal');
                }
            });
        }
        
        if (obj.escapeClose) {
            $(document).on('keydown.modal', function(event) {
                if (event.which === 27) target.trigger('close');
            });
        }
        
        if (obj.close){
            $(obj.close).click(function(){
                target.trigger('close.modal');
                return false;
            });
        }
        
        ele.on(obj.on,function(e){
            
            _SW_overlay.css({
                backgroundColor : obj.overlayColor,
                opacity : obj.overlayOpacity
            });
            
            var currentX,currentY;
            if (obj.on !== 'click'){
                $(document).one('mouseup',function(e){
                    currentX = e.pageX;
                    currentY = e.pageY;
                });
            }
            
            if (obj.position == 'center'){
                var marTop = ($(window).height()/2 - origHeight/2);
                css.margin = marTop+"px auto";
            } else if (obj.position == 'bottom'){
                var marTop = ($(window).height() - origHeight);
                css.margin = marTop+"px auto";
                //css.margin = "0 0";
            }
            
            //clear previous clone content
            clone.children(':first').hide().appendTo('body');
            target.appendTo(clone);
            
            _modalWrapper.css({
                height : $(document).height(),
                width : '100%',
                display : 'block'
            });
            
            var left = 0,
            top = $(window).scrollTop();
            target.css(css);
            var secondEasing;
            
            if (from.top == true){
                top = -((origHeight+parseInt(target.css('marginTop')))-$(window).scrollTop());
            } else if (from.bottom == true){
                top = $(window).height()+$(window).scrollTop();
            }
            
            if (from.left == true){
                var marginleft = parseInt(target.css('marginLeft'));
                if (isNaN(marginleft)){
                    marginleft = (clone.width()/2 - target.width()/2);
                }
                left = - (target.width()+marginleft);
            } else if (from.right == true){
                var marginleft = parseInt(target.css('marginLeft'));
                if (isNaN(marginleft)){
                    marginleft = (clone.width()/2 - target.width()/2);
                }
                left = (clone.width()-marginleft);
            }
            
            if (from.zoom){
                top = e.pageY;
                left = e.pageX;
                
                var marginLeft      = target.css('marginLeft')
                  , leftValue       = parseFloat(marginLeft)
                  , leftPrefix      = marginLeft.replace(leftValue,'');
                
                if (!eLeft || (leftValue == 0 && eLeft !== '0px') || eLeft == 'auto'){
                    leftValue = clone.width()/2;
                } else if (leftValue !== 0 && !isNaN(leftValue)){
                    if (leftPrefix == '%'){
                        leftValue = ($(document).width()*leftValue)/100;
                    }
                }
                
                left = left - leftValue;
                var marginTop   = target.css('marginTop')
                  , topValue    = parseFloat(marginTop)
                  , topPrefix   = marginTop.replace(topValue,'');
                
                if (topValue !== 0 && !isNaN(topValue)){
                    if (topPrefix == '%'){
                        topValue = ((clone.innerHeight()*topValue)/100);
                    }   
                    top = top - topValue;
                }
                
                //since it's a zoom effect enable size effect too
                from.width = true;
                from.height = true;
            }
            
            //noob
            var closeFunction = function(){};
            
            if (from.width == true || from.height == true){
                var cssSize = {};
                if (from.height == true){
                    cssSize.height = 0;
                } if (from.width == true){
                    cssSize.width = 0;
                }
                
                target.css(cssSize);
                var setTimer = obj.speed;
                if (isNaN(top) && isNaN(left)){
                    setTimer = 0;
                }
                
                setTimeout(function(){
                    target.animate({
                        width: origWidth,
                        height: origHeight
                    },{
                        easing : obj.easing,
                        duration : obj.speed,
                        complete : function(){}
                    });
                },setTimer);
                
                //secondEasing = 'linear';
                closeFunction = function(){
                    target.stop().animate({
                        width : 0,
                        height : 0
                    },{
                        duration : obj.speed,
                        complete : function(){
                            $(this).css({
                                width:origWidth,
                                height:origHeight
                            });
                        }
                    });
                }; 
            }
            
            target.on('close.modal',function(){
                closeFunction();
                clone.stop().animate({top : top, left : left, opacity : 0},{
                    duration : obj.speed,
                    easing: 'linear',
                    complete : function(){
                        _modalWrapper.css({
                            top : 0,
                            position : 'absolute'
                        });
                    }
                });
                
                _SW_overlay.fadeOut(obj.speed + 100,function(){
                    clone.css({
                        visibility : 'hidden',
                        top:0,
                        left:0
                    });
                    _modalWrapper.hide();
                });
                
                target.off('close.modal');
            });
            
            _SW_overlay.stop().fadeIn(obj.speed,function(){});
            
            clone.css({
                top : top,
                left: left,
                opacity: 0,
                visibility : 'visible'
            }).stop().animate({
                opacity : 1,
                top : $(window).scrollTop(),
                left: 0
            },{
                easing : secondEasing ? secondEasing : obj.easing,
                duration : obj.speed,
                complete: function(){
                    _modalWrapper.css({
                        top : -$(window).scrollTop(),
                        position : 'fixed'
                    });
                    if (obj.complete && typeof obj.complete === 'function'){
                        obj.complete();
                    }
                }
            });
            return false;
        });
    };
    
    //this is a trigger for sweefty framework if available
    if (typeof Sweefty === 'function'){
        Sweefty().trigger('modal',_modal);
    }
    
    $.fn.modal = function(action,customOptions) {  
        if (!customOptions && typeof action === 'object'){
            customOptions = action;
            action = undefined;
        }
        
        var options = $.extend({},modal_options, customOptions);
        
        options.modal = options.target;
        if (action == 'view'){
            options.modal = this;
            options.on = 'view.modal'
        } else if (action == 'close'){
            this.trigger('close.modal');
            return false;
        }
        
        return this.each(function() {
            var $this = $(this);
            if ($this[0] == $(document)[0]){
                modal_options = options;
            } else {
                _modal($this,options);
                $this.trigger('view.modal');
            }
        });
    };

}(jQuery));
