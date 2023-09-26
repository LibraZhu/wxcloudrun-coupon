import{c as tA,d as R,o as u,R as QA,n as fA,t as X,a as s,S as K,T as CA,I as oA,U as wA,w as aA,h as vA,H as P,V as hA,W as pA,X as EA,Q as IA,z as DA,Y as kA,A as v,D as h,Z as mA,C as o,M as w,_ as y,L as q,$ as M,N as S,J as Z,G as B}from"./index-392e4120.js";/* empty css              */import{B as YA}from"./index-d152ea4c.js";import{a as LA,T as FA,b as UA,S as MA}from"./index-998fa057.js";import{s as H,p as SA,c as J,b as _,d as $,g as AA,e as PA}from"./index-9a7327bb.js";import{g as GA,S as p}from"./base64-982431c5.js";import{u as xA}from"./useApp-3ee54425.js";import"./use-id-cfab6299.js";import"./use-tab-status-f6d031c7.js";const[yA,f]=tA("nav-bar"),HA={title:String,fixed:Boolean,zIndex:fA,border:X,leftText:String,rightText:String,leftDisabled:Boolean,rightDisabled:Boolean,leftArrow:Boolean,placeholder:Boolean,safeAreaInsetTop:Boolean,clickable:X};var JA=R({name:yA,props:HA,emits:["clickLeft","clickRight"],setup(e,{emit:l,slots:t}){const r=u(),E=QA(r,f),c=d=>{e.leftDisabled||l("clickLeft",d)},A=d=>{e.rightDisabled||l("clickRight",d)},Q=()=>t.left?t.left():[e.leftArrow&&s(oA,{class:f("arrow"),name:"arrow-left"},null),e.leftText&&s("span",{class:f("text")},[e.leftText])],i=()=>t.right?t.right():s("span",{class:f("text")},[e.rightText]),k=()=>{const{title:d,fixed:L,border:G,zIndex:C}=e,F=wA(C),I=e.leftArrow||e.leftText||t.left,U=e.rightText||t.right;return s("div",{ref:r,style:F,class:[f({fixed:L}),{[CA]:G,"van-safe-area-top":e.safeAreaInsetTop}]},[s("div",{class:f("content")},[I&&s("div",{class:[f("left",{disabled:e.leftDisabled}),e.clickable&&!e.leftDisabled?K:""],onClick:c},[Q()]),s("div",{class:[f("title"),"van-ellipsis"]},[t.title?t.title():d]),U&&s("div",{class:[f("right",{disabled:e.rightDisabled}),e.clickable&&!e.rightDisabled?K:""],onClick:A},[i()])])])};return()=>e.fixed&&e.placeholder?E(k):k()}});const RA=aA(JA),[eA,bA]=tA("space"),NA={align:String,direction:{type:String,default:"horizontal"},size:{type:[Number,String,Array],default:8},wrap:Boolean,fill:Boolean};function nA(e=[]){const l=[];return e.forEach(t=>{Array.isArray(t)?l.push(...t):t.type===P?l.push(...nA(t.children)):l.push(t)}),l.filter(t=>{var r;return!(t&&(t.type===hA||t.type===P&&((r=t.children)==null?void 0:r.length)===0||t.type===pA&&t.children.trim()===""))})}var VA=R({name:eA,props:NA,setup(e,{slots:l}){const t=vA(()=>{var c;return(c=e.align)!=null?c:e.direction==="horizontal"?"center":""}),r=c=>typeof c=="number"?c+"px":c,E=c=>{const A={},Q=`${r(Array.isArray(e.size)?e.size[0]:e.size)}`,i=`${r(Array.isArray(e.size)?e.size[1]:e.size)}`;return c?e.wrap?{marginBottom:i}:{}:(e.direction==="horizontal"&&(A.marginRight=Q),(e.direction==="vertical"||e.wrap)&&(A.marginBottom=i),A)};return()=>{var c;const A=nA((c=l.default)==null?void 0:c.call(l));return s("div",{class:[bA({[e.direction]:e.direction,[`align-${t.value}`]:t.value,wrap:e.wrap,fill:e.fill})]},[A.map((Q,i)=>s("div",{key:`item-${i}`,class:`${eA}-item`,style:E(i===A.length-1)},[Q]))])}}});const TA=aA(VA);const OA={class:"product-detail"},jA=["src"],zA={class:"flex p-10 bg-white"},WA={class:"product-price-after"},XA={class:"flexable"},KA={key:0,class:"product-discount-tip"},qA={key:1,class:"product-coupon-tip"},ZA=o("div",{class:"product-coupon-tip"},"券后",-1),_A={class:"product-price"},$A={class:"product-sales"},Ae={class:"product-title bg-white"},ee={class:"bg-white p-10"},te={class:"top-left flex flex-center"},oe=o("i",{class:"dot dot-top"},null,-1),ae=o("i",{class:"dot dot-bottom"},null,-1),ne=o("i",{class:"dot-divider"},null,-1),se=o("span",{class:"text"},"立即领取",-1),le={class:"product-pic"},ce=o("div",{class:"product-pic-line"},null,-1),ie=o("span",null,"宝贝详情",-1),re=[ce,ie],ge=["src"],Be=o("div",{style:{"min-height":"74px","background-color":"#f5f5f5"},class:"van-safe-area-bottom"},null,-1),de={class:"product-bottom-container van-safe-area-bottom"},ue={class:"product-bottom"},Qe=o("span",{style:{width:"40px","text-align":"center"}},"首页",-1),fe={style:{width:"40px","text-align":"center"}},me=R({__name:"ProductDetail",setup(e){var V,T,O,j,z;const l=EA(),t=IA();xA();const r=u(),E=u(document.body.clientWidth);let c=0;DA(()=>{c=document.documentElement.scrollTop||document.body.scrollTop,document.addEventListener("scroll",b)}),kA(()=>{document.removeEventListener("scroll",b)});const A=u(JSON.parse(l.query.data?GA.decode(l.query.data.toString()):"{}")),Q=u(((V=A.value.picurls)==null?void 0:V.split(","))??[]),i=parseInt(((T=l.query.type)==null?void 0:T.toString())??"0"),k=(O=l.query.id)==null?void 0:O.toString();if(k){H({message:"加载中"});const a={productId:k,source:parseInt(((j=l.query.type)==null?void 0:j.toString())??"0"),searchId:((z=l.query.sid)==null?void 0:z.toString())??""};SA(a).then(n=>{var g;J(),n.data?(A.value=n.data,Q.value=((g=n.data.picurls)==null?void 0:g.split(","))??[],$(A.value.goods_id).then(D=>{C.value=D.data??!1})):_("商品已下架")})}else $(A.value.goods_id).then(a=>{C.value=a.data??!1});const d=u(0);let L=0;const G={2:new URL("data:image/png;base64,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",self.location).href,1:new URL("data:image/png;base64,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",self.location).href,4:new URL("data:image/png;base64,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",self.location).href,3:new URL("data:image/png;base64,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",self.location).href,5:new URL("data:image/png;base64,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",self.location).href},C=u(!1),F=u({background:"linear-gradient(to right, rgba(254,129,36,0), rgba(228,81,25,0))"}),I=u("rgba(255,255,255,0)"),U=u({"background-color":"rgba(0,0,0,0.4)"}),b=()=>{c=document.documentElement.scrollTop||document.body.scrollTop;let a=Math.abs(Math.round(c))/250;F.value={background:`linear-gradient(to right, rgba(254,129,36,${a}), rgba(228,81,25,${a}))`},I.value=`rgba(255,255,255,${a})`,a>.4?U.value={"background-color":"rgba(0,0,0,0)"}:U.value={"background-color":`rgba(0,0,0,${.4-a})`},r.value.getBoundingClientRect().top<56?d.value=1:d.value=0},sA=a=>{a.name!==L&&(L=a.name,a.name===0?document.documentElement.scrollTo({top:0}):document.documentElement.scrollBy({top:r.value.getBoundingClientRect().top-46}))},lA=()=>{N()},cA=()=>{t.push({name:"home"})},iA=()=>{const a={source:A.value.source,goods_id:A.value.goods_id,goods_name:A.value.goods_name,price:A.value.price,price_after:A.value.price_after,discount:A.value.discount,picurl:A.value.picurl,salesTip:A.value.salesTip,owner:A.value.owner,rebate:A.value.rebate,searchId:A.value.searchId,discountWph:A.value.discountWph};PA(A.value.goods_id,!C.value,JSON.stringify(a)).then(n=>{C.value=n.data??!1})},N=()=>{H({message:"加载中",duration:0});const a={productId:A.value.goods_id,source:A.value.source,couponUrl:A.value.couponurl,searchId:A.value.searchId};AA(a).then(n=>{J(),i==p.JD?window.location.href=n.data:i==p.PDD?window.location.href=n.data.mobileShortUrl:i==p.TB?x(n.data,"复制成功,打开淘宝app下单"):i==p.DY?x(n.data,"复制成功,打开抖音app下单"):i==p.WPH&&(window.location.href=n.data.url)})},rA=()=>{H({message:"加载中",duration:0});const a={productId:A.value.goods_id,source:A.value.source,couponUrl:A.value.couponurl,searchId:A.value.searchId};AA(a).then(n=>{J();let g=n.data;i==p.PDD?g=n.data.mobileShortUrl:i==p.WPH&&(g=n.data.url),x(g,"内容已复制")})},x=(a,n)=>{const g=function(D){D.clipboardData.setData("text/plain",a),D.preventDefault()};document.addEventListener("copy",g,{once:!0}),document.execCommand("copy"),_(n)};return(a,n)=>{const g=FA,D=UA,gA=RA,BA=MA,dA=LA,m=oA,uA=TA,W=YA;return B(),v("div",OA,[s(gA,{title:"商品详情","left-arrow":!1,fixed:"",class:"navbar",style:mA(F.value)},{title:h(()=>[s(D,{active:d.value,"onUpdate:active":n[0]||(n[0]=Y=>d.value=Y),onClickTab:sA,"line-width":"20",color:I.value,"title-active-color":I.value,"title-inactive-color":I.value,background:"rgba(0,0,0,0)"},{default:h(()=>[s(g,{title:"宝贝",name:0}),s(g,{title:"详情",name:1})]),_:1},8,["active","color","title-active-color","title-inactive-color"])]),_:1},8,["style"]),s(dA,{width:E.value,height:E.value},{default:h(()=>[(B(!0),v(P,null,Z(Q.value,Y=>(B(),M(BA,null,{default:h(()=>[o("img",{style:{width:"100%"},src:Y},null,8,jA)]),_:2},1024))),256))]),_:1},8,["width","height"]),o("div",zA,[o("span",WA,"¥"+w(A.value.price_after),1),o("div",XA,[A.value.source===5?(B(),v("div",KA," 折扣价 ")):A.value.discount!="0"?(B(),v("div",qA," 券后 ")):y("",!0),ZA,o("div",_A,"原价¥"+w(A.value.price),1)]),o("span",$A,w(A.value.salesTip),1)]),o("div",Ae,[q(i)?(B(),M(m,{key:0,name:G[q(i)]},null,8,["name"])):y("",!0),S(" "+w(A.value.goods_name),1)]),o("div",ee,[A.value.discount!="0"?(B(),v("div",{key:0,class:"product-coupon flex bg-white",onClick:lA},[o("div",te,[oe,ae,ne,S(" "+w(A.value.discount)+"元优惠券 ",1)]),se,s(m,{name:"arrow",color:"#fff"})])):y("",!0)]),o("div",le,[o("div",{ref_key:"tabRef",ref:r,class:"product-pic-title"},re,512),(B(!0),v(P,null,Z(Q.value,Y=>(B(),v("img",{style:{width:"100%"},src:Y},null,8,ge))),256))]),Be,o("div",de,[o("div",ue,[s(uA,{size:0},{default:h(()=>[o("div",{class:"product-to-home",onClick:cA},[s(m,{size:"20",name:"wap-home-o"}),Qe]),o("div",{class:"product-to-home",onClick:iA},[C.value?(B(),M(m,{key:0,size:"20",name:"star",color:"#fe8124"})):(B(),M(m,{key:1,size:"20",name:"star-o"})),o("span",fe,w(C.value?"已收藏":"收藏"),1)])]),_:1}),s(W,{round:"",color:"#ffcd6d",style:{"margin-left":"10px",color:"#333",flex:"1"},onClick:rA},{default:h(()=>[S(w(`分享赚¥${A.value.rebate??""}`),1)]),_:1}),s(W,{round:"",color:"#e45119",style:{"margin-left":"10px",flex:"1"},onClick:N},{default:h(()=>[S(w(`下单返¥${A.value.rebate??""}`),1)]),_:1})])])])}}});export{me as default};
