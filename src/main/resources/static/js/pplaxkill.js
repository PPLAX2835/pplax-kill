//存放主要交互逻辑js代码
// javascript 模块化
var pplaxKill = {
    VAL: {
        killId: 0,
        intervX: 0
    },
    //封装秒杀相关ajax的url
    URL: {
        now: function () {
            return '/pplaxKill/time/now';
        },
        exposer: function (killId) {
            return '/pplaxKill/exposer/' + killId;
        },
        execution: function (killId, phone, md5) {
            return '/pplaxKill/execution/' + killId + '/' + phone + '/' + md5;
        },
        isGrab: function (killId, phone) {
            return '/pplaxKill/isGrab/' + killId + '/' + phone;
        }
    },
    handleSeckillkill: function (killId, node) {
        //获取秒杀地址，控制显示逻辑 ，执行秒杀
        node.hide()
            .html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');//按钮
        $.post(pplaxKill.URL.exposer(killId),
            {},
            function (result) {
                //在回调函数中，执行交互流程
                if (result && result['success']) {
                    var exposer = result['data'];
                    if (exposer['exposed']) {
                        //开启秒杀
                        //获取秒杀地址.
                        var md5 = exposer['md5'];
                        pplaxKill.VAL.killId = killId;
                        var currentPhone = $.cookie('killPhone');
                        var killUrl = pplaxKill.URL.execution(killId, currentPhone, md5);
                        console.log("killUrl:" + killUrl);
                        //绑定一次点击事件
                        $('#killBtn').one('click', function () {
                            //执行秒杀请求
                            //1:先禁用按钮
                            $(this).addClass('disabled');
                            //2:发送秒杀请求执行秒杀
                            $.post(killUrl, {}, function (result) {
                                if (result && result['success']) {
                                    var killResult = result['data'];
                                    var state = killResult['state'];
                                    var stateInfo = killResult['stateInfo'];
                                    //3:显示秒杀结果
                                    node.html('<span class="label label-success">' + stateInfo + '</span>');

                                    if (result.data.state === 6) {
                                        pplaxKill.VAL.intervX = window.setInterval(pplaxKill.isGrab, 1000);
                                    }

                                }
                            });
                        });
                        node.show();
                    } else {
                        //未开启秒杀,
                        var now = exposer['now'];
                        var start = exposer['start'];
                        var end = exposer['end'];
                        //重新计算计时逻辑
                        pplaxKill.countdown(killId, now, start, end);
                    }
                } else {
                    console.log('result:' + result);
                }

            });
    },
    //验证手机号
    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },
    countdown: function (killId, nowTime, startTime, endTime) {
        var pplaxKillBox = $('#pplaxKill-box');
        //时间判断
        if (nowTime > endTime) {
            //秒杀结束
            pplaxKillBox.html('秒杀结束!');
        } else if (nowTime < startTime) {
            //秒杀未开始,计时事件绑定
            var killTime = new Date(startTime + 1000);
            pplaxKillBox.countdown(killTime, function (event) {
                //时间格式
                var format = event.strftime('秒杀倒计时: %D天 %H时 %M分 %S秒');
                pplaxKillBox.html(format);
                /*时间完成后回调事件*/
            }).on('finish.countdown', function () {

                pplaxKill.handleSeckillkill(killId, pplaxKillBox);
            });
        } else {
            //秒杀开始
            pplaxKill.handleSeckillkill(killId, pplaxKillBox);
        }
    },
    //详情页秒杀逻辑
    detail: {
        //详情页初始化
        init: function (params) {
            //手机验证和登录 , 计时交互
            //规划我们的交互流程
            //在cookie中查找手机号
            var killPhone = $.cookie('killPhone');
            //验证手机号
            if (!pplaxKill.validatePhone(killPhone)) {
                //绑定phone
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                //显示弹出层
                killPhoneModal.modal({
                    show: true,//显示弹出层
                    backdrop: 'static',//禁止位置关闭
                    keyboard: false//关闭键盘事件
                });
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    console.log('inputPhone=' + inputPhone);//TODO
                    if (pplaxKill.validatePhone(inputPhone)) {
                        //电话写入cookie
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/pplaxKill'});
                        //刷新页面
                        window.location.reload();
                    } else {
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }
                });
            }
            //已经登录
            //计时交互
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var killId = params['killId'];
            $.get(pplaxKill.URL.now(), {}, function (result) {
                if (result && result['success']) {
                    var nowTime = result['data'];
                    //时间判断,计时交互
                    pplaxKill.countdown(killId, nowTime, startTime, endTime);
                } else {
                    console.log('result:' + result);
                }
            });


        }
    },
    isGrab: function () {
        var node = $('#pplaxKill-box');
        var currentPhone = $.cookie('killPhone');
        $.post(pplaxKill.URL.isGrab(pplaxKill.VAL.killId, currentPhone),
            {},
            function (result) {
                if (result == 0) {
                    console.log(">>>>秒杀排队中...");
                    node.html('<span class="label label-success">' + "排队中..." + '</span>');
                } else {
                    if (pplaxKill.VAL.intervX != 0) {
                        window.clearInterval(pplaxKill.VAL.intervX);
                    }

                    if (result == 1) {
                        console.log(">>>>秒杀成功");
                        node.html('<span class="label label-success">' + "秒杀成功" + '</span>');
                    } else if (result == 2) {
                        console.log(">>>>没抢到！");
                        node.html('<span class="label label-success">' + "没抢到" + '</span>');
                    }
                }

            });
    }

}