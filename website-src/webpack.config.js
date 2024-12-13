const HtmlWebpackPlugin = require("html-webpack-plugin");
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const path = require("path");
const webpack = require("webpack");
const { VueLoaderPlugin } = require('vue-loader');

const htmls = [];

for (let i = 0; i < 2; i++) {
    htmls.push(new HtmlWebpackPlugin({
        filename: `blog/${i + 1}.html`,
        template: `./public/blog/${i + 1}.html`,
        chunks: ['blog']
    }));
}

module.exports = (env, argv) => {
    const isProduction = env.production;

    return {
        mode: 'production', // 设置为 production|development 模式
        entry: {
            index: "./src/index.js",
            docs: "./src/docs.js",
            blog: "./src/blog.js",
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
                filename: "index.html",
                template: "./public/index.html",
                chunks: ['index']
            }),

            new HtmlWebpackPlugin({
                filename: "docs.html",
                template: "./public/blog.html",
                chunks: ['docs']
            }),

            new HtmlWebpackPlugin({
                filename: "blog.html",
                template: "./public/blog.html",
                chunks: ['blog']
            }),
            ...htmls,

            new VueLoaderPlugin(),

        ],
        resolve: {  //resolve 字段最常用的就是 alias （别名）属性，用来把一些冗长的路径替换为简单的字符，以便 js 中引入模块时更简洁
            alias: {
                '@': path.resolve(__dirname, 'src'),
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

                {
                    test: /\.vue$/,
                    loader: 'vue-loader'
                },

                {// css 作为资源文件，修改为 css2 避免与 css loader 冲突
                    test: /(wechatlogin|reset_pwd_H5|sso_form)\.css2$/i,
                    type: "asset/resource",
                },
                {
                    test: /\.less$/i, // 匹配.less文件
                    use: [
                        'style-loader', // 将CSS添加到DOM中
                        'css-loader', // 将CSS转换为CommonJS模块
                        'less-loader' // 将LESS编译为CSS
                    ]
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