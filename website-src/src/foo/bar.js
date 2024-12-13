define(['jquery'], function ($) {
    console.log('bar');
    
    return {
        name: 'bar',
        init: function () {
            console.log('bar init');
        }
    }
});