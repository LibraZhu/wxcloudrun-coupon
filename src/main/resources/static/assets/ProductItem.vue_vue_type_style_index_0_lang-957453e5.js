import{d as f,Q as g,X as v,G as e,A as d,a as n,C as t,$ as l,N as p,M as c,_ as k,D as B,E as I}from"./index-47e2b40a.js";/* empty css              */import{B as b}from"./index-59155b31.js";import{I as C}from"./index-84671b39.js";import{S as _,g as D}from"./base64-982431c5.js";const N={class:"product-content"},P={class:"product-title van-multi-ellipsis--l2"},V={key:0,style:{display:"inline-block"}},w={class:"product-discount"},S={"wx:if":"{{product.discount!=='0'}}",class:"product-coupon"},q={key:1,class:"product-coupon"},E={key:2,class:"product-rebate"},T={class:"flex"},z={class:"product-price"},J={class:"product-sales"},R=t("div",{class:"product-divider"},[t("div")],-1),W={key:0,class:"product-price-after-tip"},j={key:1,class:"product-price-after-tip"},A={class:"product-price-after"},G=t("span",null,"¥",-1),H=f({__name:"ProductItem",props:{product:{},from:{}},setup(m){const s=m,u=g();v();const{assets:r}=I(),a={2:r("jd.png"),1:r("pdd.png"),4:r("dy.png"),3:r("tb.png"),5:r("wph.png"),33:r("tm.png")},h=()=>{if(s.from==="collect"){u.push({name:"ProductDetail",query:{id:s.product.goods_id,sid:s.product.searchId,type:s.product.source}});return}s.product.source==_.JD||s.product.source==_.TB?u.push({name:"ProductDetail",query:{data:D.encode(JSON.stringify(s.product)),type:s.product.source}}):u.push({name:"ProductDetail",query:{id:s.product.goods_id,sid:s.product.searchId,type:s.product.source}})};return(o,M)=>{const i=C,y=b;return e(),d("div",{class:"product-item",onClick:h},[n(i,{width:"150",height:"150",radius:"8","lazy-load":"",src:o.product.picurl},null,8,["src"]),t("div",N,[t("div",P,[o.product.is_tmall?(e(),l(i,{key:0,height:"14",src:a[33]},null,8,["src"])):(e(),l(i,{key:1,width:"16",src:a[o.product.source]},null,8,["src"])),p(" "+c(o.product.goods_name),1)]),t("div",null,[o.product.discountWph?(e(),d("div",V,[t("div",w,c(o.product.discountWph)+"折",1),t("div",S,c(o.product.discount)+"元券 ",1)])):(e(),d("div",q,c(o.product.discount||"0")+"元券 ",1)),o.product.rebate&&o.product.rebate!=="0"?(e(),d("div",E," 返¥"+c(o.product.rebate),1)):k("",!0)]),t("div",T,[t("span",z,"原价"+c(o.product.price),1),t("span",J,c(o.product.salesTip),1)]),R,o.product.source==5?(e(),d("div",W," 折扣价 ")):(e(),d("div",j,"券后价")),t("div",A,[G,p(c(o.product.price_after),1)]),n(y,{class:"product-button",color:"#f9441c",size:"small"},{default:B(()=>[p(" 领券抢购 ")]),_:1})])])}}});export{H as _};