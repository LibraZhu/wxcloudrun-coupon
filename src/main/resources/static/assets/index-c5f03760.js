import{c as z,d as D,u as ce,a,b as O,B as ie,t as G,n as U,m as le,w as j,e as K,r as ue,f as de,g as me,h as H,i as pe,j as Y,k as fe,I as ge,l as M,o as y,p as ve,q as J,s as he,v as be,x as _e,y as ke,z as ye,A as C,C as x,D as b,E as xe,F,G as T,H as w,J as R,K as B}from"./index-392e4120.js";/* empty css              */import{P as Pe}from"./index-a1c98717.js";import{L as Ie}from"./index-d3311b8c.js";import{S as Ce,a as Te,T as Se,b as Le}from"./index-998fa057.js";import"./index-ba0ce003.js";import{f as Q,F as Ve}from"./index-930dc4b1.js";import{l as Ee,s as Ne,u as we,c as Re}from"./index-9a7327bb.js";import{S as Be}from"./base64-982431c5.js";import{u as $e}from"./useApp-3ee54425.js";import{_ as De}from"./ProductItem.vue_vue_type_style_index_0_lang-08a7fa67.js";import{u as qe}from"./use-id-cfab6299.js";import"./use-tab-status-f6d031c7.js";import"./index-d152ea4c.js";const[W,Ae]=z("grid"),Fe={square:Boolean,center:G,border:G,gutter:U,reverse:Boolean,iconSize:U,direction:String,clickable:Boolean,columnNum:le(4)},X=Symbol(W);var Oe=D({name:W,props:Fe,setup(t,{slots:e}){const{linkChildren:s}=ce(X);return s({props:t}),()=>{var p;return a("div",{style:{paddingLeft:O(t.gutter)},class:[Ae(),{[ie]:t.border&&!t.gutter}]},[(p=e.default)==null?void 0:p.call(e)])}}});const Ge=j(Oe),[Ue,$]=z("grid-item"),ze=K({},ue,{dot:Boolean,text:String,icon:String,badge:U,iconColor:String,iconPrefix:String,badgeProps:Object});var je=D({name:Ue,props:ze,setup(t,{slots:e}){const{parent:s,index:p}=de(X),f=me();if(!s)return;const m=H(()=>{const{square:c,gutter:r,columnNum:u}=s.props,v=`${100/+u}%`,d={flexBasis:v};if(c)d.paddingTop=v;else if(r){const h=O(r);d.paddingRight=h,p.value>=+u&&(d.marginTop=h)}return d}),g=H(()=>{const{square:c,gutter:r}=s.props;if(c&&r){const u=O(r);return{right:u,bottom:u,height:"auto"}}}),P=()=>{if(e.icon)return a(fe,Y({dot:t.dot,content:t.badge},t.badgeProps),{default:e.icon});if(t.icon)return a(ge,{dot:t.dot,name:t.icon,size:s.props.iconSize,badge:t.badge,class:$("icon"),color:t.iconColor,badgeProps:t.badgeProps,classPrefix:t.iconPrefix},null)},I=()=>{if(e.text)return e.text();if(t.text)return a("span",{class:$("text")},[t.text])},S=()=>e.default?e.default():[P(),I()];return()=>{const{center:c,border:r,square:u,gutter:v,reverse:d,direction:h,clickable:_}=s.props,k=[$("content",[h,{center:c,square:u,reverse:d,clickable:_,surround:r&&v}]),{[pe]:r}];return a("div",{class:[$({square:u})],style:m.value},[a("div",{role:_?"button":void 0,class:k,style:g.value,tabindex:_?0:void 0,onClick:f},[S()])])}}});const Ke=j(je),[He,L,Me]=z("search"),Je=K({},Q,{label:String,shape:M("square"),leftIcon:M("search"),clearable:G,actionText:String,background:String,showAction:Boolean});var Ye=D({name:He,props:Je,emits:["blur","focus","clear","search","cancel","clickInput","clickLeftIcon","clickRightIcon","update:modelValue"],setup(t,{emit:e,slots:s,attrs:p}){const f=qe(),m=y(),g=()=>{s.action||(e("update:modelValue",""),e("cancel"))},P=n=>{n.keyCode===13&&(he(n),e("search",t.modelValue))},I=()=>t.id||`${f}-input`,S=()=>{if(s.label||t.label)return a("label",{class:L("label"),for:I()},[s.label?s.label():t.label])},c=()=>{if(t.showAction){const n=t.actionText||Me("cancel");return a("div",{class:L("action"),role:"button",tabindex:0,onClick:g},[s.action?s.action():n])}},r=()=>{var n;return(n=m.value)==null?void 0:n.blur()},u=()=>{var n;return(n=m.value)==null?void 0:n.focus()},v=n=>e("blur",n),d=n=>e("focus",n),h=n=>e("clear",n),_=n=>e("clickInput",n),k=n=>e("clickLeftIcon",n),q=n=>e("clickRightIcon",n),V=Object.keys(Q),A=()=>{const n=K({},p,J(t,V),{id:I()}),i=l=>e("update:modelValue",l);return a(Ve,Y({ref:m,type:"search",class:L("field"),border:!1,onBlur:v,onFocus:d,onClear:h,onKeypress:P,onClickInput:_,onClickLeftIcon:k,onClickRightIcon:q,"onUpdate:modelValue":i},n),J(s,["left-icon","right-icon"]))};return ve({focus:u,blur:r}),()=>{var n;return a("div",{class:L({"show-action":t.showAction}),style:{background:t.background}},[(n=s.left)==null?void 0:n.call(s),a("div",{class:L("content",t.shape)},[S(),A()]),c()])}}});const Qe=j(Ye);const We={class:"tabbar-container home"},Xe=x("div",{class:"top-bg"},null,-1),Ze={class:"navbar"},et=x("span",{class:"tip"},"多多省助手",-1),tt=["src"],nt=["src"],ot={class:"product-container"},at=x("div",{class:"divider-list"},null,-1),_t=D({name:"Home",__name:"index",setup(t){$e();const{assets:e}=xe(),s=y("购物搜券拿返利"),p=y(2),f=y(!1),m=y(!1),g=y(!1),P=[{id:2,name:"精选"},{id:23,name:"为你推荐"},{id:22,name:"热销"},{id:10,name:"9.9包邮"},{id:26,name:"母婴"},{id:28,name:"美妆穿搭"},{id:30,name:"图书"},{id:27,name:"居家"},{id:25,name:"超市"}],I=[{image:e("banner.png"),type:"search"}],S=[{image:e("hot.png"),type:"hot",name:"今日热卖",routeName:"Hot"},{image:e("jd.png"),type:2,name:"京东优惠",routeName:"ProductList"},{image:e("pdd.png"),type:1,name:"拼多多",routeName:"ProductList"},{image:e("tb.png"),type:3,name:"淘宝优惠",routeName:"ProductList"},{image:e("wph.png"),type:5,name:"唯品会",routeName:"ProductList"},{image:e("dy.png"),type:4,name:"抖音优惠",routeName:"ProductList"},{image:e("mt.png"),type:"mt",name:"美团外卖",routeName:"MeiTuan"},{image:e("ele.png"),type:"ele",name:"饿了么",routeName:"Ele"},{image:e("didi.png"),type:"didi",name:"滴滴打车",routeName:"Taxi"},{image:e("movie.png"),type:"movie",name:"特价电影"}],c=y([]),r=be({optId:P[0].id,source:Be.JD,page:1,pageSize:20});let u=0;_e(()=>{document.documentElement.scrollTop=u,B.on("backTop",v),document.addEventListener("scroll",d)}),ke(()=>{B.off("backTop",v),document.removeEventListener("scroll",d)});const v=()=>{document.documentElement.scrollTo({left:0,top:0,behavior:"smooth"})},d=()=>{u=document.documentElement.scrollTop,document.documentElement.scrollTop>500?B.emit("scrollTop",!0):B.emit("scrollTop",!1)},h=i=>{if(i.type==="movie"){Ne({message:"加载中",duration:0}),we(3).then(l=>{Re(),setTimeout(()=>{window.open(l.data.h5??"")},100)});return}i.routeName==="ProductList"?F.push({name:"ProductList",query:{type:i.type}}):F.push({name:i.routeName})},_=i=>{r.optId=i.name,k()},k=()=>{g.value=!0,f.value=!1,m.value=!0,r.page=1,V()},q=()=>{c.value.length!==0&&(r.page++,V())},V=()=>{Ee(r).then(i=>{var E,N;const l=((E=i.data)==null?void 0:E.list)??[];g.value=!1,f.value=!1,r.page===1?c.value=l:c.value=c.value.concat(l),m.value=c===((N=i.data)==null?void 0:N.total)}).catch(i=>{r.page>1&&r.page--,g.value=!1,f.value=!1,m.value=!0})};ye(()=>{k()});const A=i=>{},n=()=>{F.push({name:"Search"})};return(i,l)=>{const E=Qe,N=Ce,Z=Te,ee=Ke,te=Ge,ne=Se,oe=Le,ae=Ie,se=Pe;return T(),C("div",We,[Xe,x("div",Ze,[a(E,{modelValue:s.value,"onUpdate:modelValue":l[0]||(l[0]=o=>s.value=o),class:"search",readonly:"",shape:"round",background:"linear-gradient(to right, #fe8124, #e45119)",placeholder:"",onClickInput:n},{left:b(()=>[et]),_:1},8,["modelValue"])]),a(se,{ref:"pullRefreshRef",class:"pullrefresh",modelValue:g.value,"onUpdate:modelValue":l[3]||(l[3]=o=>g.value=o),onRefresh:k},{default:b(()=>[a(Z,{autoplay:3e3,"indicator-color":"white",class:"banner"},{default:b(()=>[(T(),C(w,null,R(I,o=>a(N,{key:o.image,style:{"border-radius":"10px"},onClick:re=>A(o)},{default:b(()=>[x("img",{style:{width:"100%","border-radius":"10px"},src:o.image},null,8,tt)]),_:2},1032,["onClick"])),64))]),_:1}),a(te,{"column-num":5,border:!1},{default:b(()=>[(T(),C(w,null,R(S,o=>a(ee,{key:o.type,text:o.name,onClick:re=>h(o)},{icon:b(()=>[x("img",{style:{width:"36px",height:"36px"},src:o.image},null,8,nt)]),_:2},1032,["text","onClick"])),64))]),_:1}),x("div",ot,[a(oe,{active:p.value,"onUpdate:active":l[1]||(l[1]=o=>p.value=o),onClickTab:_,"line-width":"20",color:"#fe8124","title-active-color":"#fe8124"},{default:b(()=>[(T(),C(w,null,R(P,o=>a(ne,{title:o.name,key:o.id,name:o.id},null,8,["title","name"])),64))]),_:1},8,["active"]),a(ae,{loading:f.value,"onUpdate:loading":l[2]||(l[2]=o=>f.value=o),finished:m.value,"finished-text":c.value.length>0?"没有更多了":"",onLoad:q},{default:b(()=>[(T(!0),C(w,null,R(c.value,o=>(T(),C("div",{style:{position:"relative"},key:o.goods_id},[a(De,{product:o},null,8,["product"]),at]))),128))]),_:1},8,["loading","finished","finished-text"])])]),_:1},8,["modelValue"])])}}});export{_t as default};
