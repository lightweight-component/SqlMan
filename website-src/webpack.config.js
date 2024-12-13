const HtmlWebpackPlugin = require("html-webpack-plugin");
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const path = require("path");
const webpack = require("webpack");

module.exports = (env, argv) => {
    const isProduction = env.production;

    return {
        mode: 'production', // 设置为 production|development 模式
        entry: {
            index: "./src/main.js",
            app_download: "./src/new_js/app_download.js",
            weixin: "./src/new_js/weixin.js",
            authFail: "./src/new_js/authFail.js",
            dingtalklogin: "./src/new_js/dingtalklogin.js",
            fail: "./src/new_js/fail.js",
            finish: "./src/new_js/finish.js",
            otp: "./src/new_js/otp.js",
            qrcode: "./src/new_js/qrcode.js",
            reset_initOrExp_pwd: "./src/new_js/reset_initOrExp_pwd.js",
            reset_pwd: "./src/new_js/reset_pwd.js",
            selectOrg: "./src/new_js/selectOrg.js",
        },
        output: {
            filename: "[name].js",
            path: path.resolve(__dirname, "./dist"),
            clean: true // Automatically Clean Webpack’s Output Directory
            // assetModuleFilename: 'images/[name][ext][query]'
            // assetModuleFilename: 'images/[hash][ext][query]'
        },
        plugins: [
            new MiniCssExtractPlugin({
                filename: isProduction ? 'css/[name].[contenthash].css' : 'css/[name].css', // 生产模式下使用内容哈希
                chunkFilename: "css/[name].[contenthash].chunk.css",
            }),
            new HtmlWebpackPlugin({
                filename: "app_download.html",
                template: "./public/app_download.html",
                chunks: ['app_download']
            }),
            new HtmlWebpackPlugin({
                filename: "index.html",
                template: "./public/index.html",
                chunks: ['index']
            }),
            new HtmlWebpackPlugin({
                filename: "authFail.html",
                template: "./public/authFail.html",
                chunks: ['authFail']
            }),
            new HtmlWebpackPlugin({
                filename: "dingtalklogin.html",
                template: "./public/dingtalklogin.html",
                chunks: ['dingtalklogin']
            }),
            new HtmlWebpackPlugin({
                filename: "fail.html",
                template: "./public/fail.html",
                chunks: ['fail']
            }),
            new HtmlWebpackPlugin({
                filename: "feishu.html",
                template: "./public/feishu.html",
                chunks: []
            }),
            new HtmlWebpackPlugin({
                filename: "finish.html",
                template: "./public/finish.html",
                chunks: ['finish']
            }),
            new HtmlWebpackPlugin({
                filename: "otp.html",
                template: "./public/otp.html",
                chunks: ['otp']
            }),
            new HtmlWebpackPlugin({
                filename: "qrcode.html",
                template: "./public/qrcode.html",
                chunks: ['qrcode']
            }),
            new HtmlWebpackPlugin({
                filename: "reset_initOrExp_pwd.html",
                template: "./public/reset_initOrExp_pwd.html",
                chunks: ['reset_initOrExp_pwd']
            }),
            new HtmlWebpackPlugin({
                filename: "reset_pwd.html",
                template: "./public/reset_pwd.html",
                chunks: ['reset_pwd']
            }),
            new HtmlWebpackPlugin({
                filename: "selectOrg.html",
                template: "./public/selectOrg.html",
                chunks: ['selectOrg']
            }),
            new HtmlWebpackPlugin({
                filename: "weixin.html",
                template: "./public/weixin.html",
                chunks: ['weixin']
            }),

            new webpack.ProvidePlugin({
                $: 'jquery',
                jQuery: 'jquery',
            }),

        ],
        resolve: {  //resolve 字段最常用的就是 alias （别名）属性，用来把一些冗长的路径替换为简单的字符，以便 js 中引入模块时更简洁
            alias: {
                '@': path.resolve(__dirname, 'src'),
                newIndex: path.resolve(__dirname, 'src/new_js/index.js'),
                i18n: path.resolve(__dirname, 'src/app/language'),
                jsbn: path.resolve(__dirname, 'src/lib/jsbn'),
                smCrypto: path.resolve(__dirname, 'src/lib/sm-crypto'),
                api: path.resolve(__dirname, 'src/app/api'),
                util: path.resolve(__dirname, 'src/app/util'),
                artTemplate: path.resolve(__dirname, 'src/lib/template'),
                context: path.resolve(__dirname, 'src/app/context'),
                log: path.resolve(__dirname, 'src/app/log'),
                auth: path.resolve(__dirname, 'src/app/auth'),
                encrypt: path.resolve(__dirname, 'src/app/encrypt'),
                jsEncrypt: path.resolve(__dirname, 'src/lib/jsencrypt'),
                msg: path.resolve(__dirname, 'src/app/msg'),
                layer: path.resolve(__dirname, 'src/lib/layer/layer'),
                issue: path.resolve(__dirname, 'src/app/issue'),
                dingtalk: path.resolve(__dirname, 'src/lib/dingtalk.open'),
                placeholder: path.resolve(__dirname, 'src/lib/jquery.placeholder.min'),
                feishu: path.resolve(__dirname, 'src/lib/feishu.open'),
                feishuqrcode: path.resolve(__dirname, 'src/lib/feishuqrcode'),
                wxLogin: path.resolve(__dirname, 'src/lib/wxLogin'),
                CimsBase64: path.resolve(__dirname, 'src/js/base64Util'),
            }
        },
        module: {
            rules: [
                {
                    test: /\.ejs$/,
                    loader: "ejs-loader",
                    // use: 'ejs-compiled-loader',
                    options: {
                        esModule: false,
                    },
                },
                {// css 作为资源文件，修改为 css2 避免与 css loader 冲突
                    test: /(wechatlogin|reset_pwd_H5|sso_form)\.css2$/i,
                    type: "asset/resource",
                },
                {
                    test: /\.css$/i,
                    use: [isProduction ? MiniCssExtractPlugin.loader : 'style-loader', "css-loader",],
                },
                {// i18n 资源文件
                    test: /\.(properties)$/i,
                    type: "asset/resource",
                },
                {// 图片处理
                    test: /\.(png|jpg|svg|jpeg|gif|ico)$/i,
                    type: "asset/resource",
                    parser: { // 如果一个模块源码大小小于 maxSize，那么模块会被作为一个 Base64 编码的字符串注入到包中， 否则模块文件会被生成到输出的目标目录中。
                        dataUrlCondition: {
                            maxSize: 40 * 1024
                        }
                    },
                    generator: {
                        filename: 'images/[hash][ext][query]' // [name] 会报错
                    }
                },
                // { test: /.html$/, loader: 'html-loader' }// 修正 HTML 加载 img src 图片 与 ejs 冲突
            ],
        },
        performance: {
            hints: 'warning', // 或者 'error' 或 false
            maxAssetSize: 1000000, // 最大单个文件大小（以字节为单位）
            maxEntrypointSize: 2000000, // 最大入口点大小（以字节为单位）
            assetFilter: (assetFilename) => { // 忽略某些文件类型的警告
                return !/(\.map$)|(^(runtime|polyfills|styles)\.bundle\.js$)/.test(assetFilename);
            },
        },
        optimization: {
            minimize: isProduction, // 只有在生产模式下才启用压缩
            minimizer: [
                // 在 webpack@5.0.0 中，你可以使用 `...` 语法来扩展默认的 minimizer（terser-webpack-plugin）
                `...`,
                new CssMinimizerPlugin(), // 压缩 CSS
            ],
            // splitChunks: {
            //   cacheGroups: {
            //     vendor: {
            //       test: /[\\/]node_modules[\\/]/,
            //       name: 'vendors',
            //       chunks: 'all',
            //     },
            //   },
            // },
            // 分包
            splitChunks: {
                minSize: 5 * 1024,
                chunks: 'all',
                name: 'my-common',
                automaticNameDelimiter: '_',
                cacheGroups: {
                    'my-common': {
                        name: 'my-common',
                        chunks: 'all',
                        test: /my-common\.js/,
                    },
                },
            },
        },
        devtool: isProduction ? false : 'source-map',
        devServer: {
            static: {
                directory: path.resolve(__dirname, '../dist'),
            },
            watchFiles: ['public/**/*'],
            compress: true,
            port: 8000,
            open: false, // 是否自动打开浏览器
            hot: true,
            proxy:
                [ // 配置代理（只在本地开发有效，上线无效）
                    {
                        context: ['/api'],
                        target: 'http://10.19.240.1:8100/authn',
                        changeOrigin: true
                    }
                ]
        },
    };
};