import './style/reset.css';
import './style/common.less';

import img from '../asset/img.jpg';
import imgDark from '../asset/img-drak.jpg';
import MySwitch from './components/Switch.vue';

new Vue({
    el: '#tools',
    components: {
        MySwitch
    },
    data: {
        isDark: false,
        message: 'Hello Vue!'
    },
    watch: {
        isDark() {
            // document.body.classList.toggle('dark', this.isDark);
            // if(this.isDark)
            console.log(this.isDark);
            document.querySelector("#main-img").src = this.isDark ? imgDark : img;
            document.body.classList.toggle('color-scheme-drak', this.isDark);
        }
        // main-img
    }
});