import{d as L,o as u,z as k,$ as f,D as v,G as c,a as g,A as _,J as x,H as C,_ as y,C as P}from"./index-392e4120.js";/* empty css              */import{P as B}from"./index-a1c98717.js";import{E}from"./index-9a47d5f2.js";import{L as N}from"./index-d3311b8c.js";import{q as R}from"./index-9a7327bb.js";import{u as w}from"./useApp-3ee54425.js";import{_ as z}from"./ProductItem.vue_vue_type_style_index_0_lang-08a7fa67.js";import"./use-id-cfab6299.js";import"./use-tab-status-f6d031c7.js";import"./index-d152ea4c.js";import"./base64-982431c5.js";const A=P("div",{class:"divider-list"},null,-1),K=L({name:"Collect",__name:"index",setup(M){w();const t={page:1,pageSize:20},e=u([]),o=u(!1),s=u(!1),n=u(!1),p=()=>{o.value=!0,s.value=!1,n.value=!0,t.page=1,m()},h=()=>{e.value.length<t.page||(t.page++,m())},m=()=>{R(t).then(d=>{var r,i;const a=((r=d.data)==null?void 0:r.list)??[];t.page===1?e.value=a:e.value=e.value.concat(a),o.value=!1,s.value=!1,n.value=e.value.length>=((i=d.data)==null?void 0:i.total)}).catch(()=>{o.value=!1,s.value=!1,n.value=!0,t.page>1&&t.page--})};return k(()=>{p()}),(d,a)=>{const r=N,i=E,V=B;return c(),f(V,{class:"page-container",modelValue:o.value,"onUpdate:modelValue":a[1]||(a[1]=l=>o.value=l),onRefresh:p},{default:v(()=>[g(r,{loading:s.value,"onUpdate:loading":a[0]||(a[0]=l=>s.value=l),finished:n.value,"finished-text":e.value.length>0?"没有更多了":"",onLoad:h},{default:v(()=>[(c(!0),_(C,null,x(e.value,l=>(c(),_("div",{style:{position:"relative","background-color":"white"},key:l.goods_id},[g(z,{from:"collect",product:l},null,8,["product"]),A]))),128))]),_:1},8,["loading","finished","finished-text"]),!o.value&&e.value.length===0?(c(),f(i,{key:0,description:"还没有数据~"})):y("",!0)]),_:1},8,["modelValue"])}}});export{K as default};
