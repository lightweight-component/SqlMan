import '../css/normalize.css';
import '../css/mobile.css';
import '../css/font_login/iconfont.css';

import 'jquery';
import dd from '../lib/dingtalk.open.js';
import { dingtalkBackListening } from './my-common';

dd.ready(function () { // 钉钉
    dingtalkBackListening();
});

