<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>
  <title>基础数据上传</title>
  <script src="${pageContext.request.contextPath}/js/jquery-1.9.1.js"></script>
  <style type="text/css">
    .bu {
      text-decoration: none;
      background: #4dc86f;
      color: #f2f2f2;
      padding: 10px 30px 10px 30px;
      font-size: 16px;
      font-family: 微软雅黑, 宋体, Arial, Helvetica, Verdana, sans-serif;
      font-weight: bold;
      border-radius: 3px;
      -webkit-transition: all linear 0.30s;
      -moz-transition: all linear 0.30s;
      transition: all linear 0.30s;
    }

    table {
      border-collapse: collapse;
      margin: 0 auto;
      text-align: center;
    }

    table td, table th {
      border: 1px solid #cad9ea;
      color: #666;
      height: 30px;
    }

    table thead th {
      background-color: #CCE8EB;
      width: 100px;
    }

    table tr:nth-child(odd) {
      background: #fff;
    }

    table tr:nth-child(even) {
      background: #F5FAFA;
    }
  </style>
</head>

<body style="text-align: center;">
<div style="margin-top: 30px;">
  <button onclick="uploadUnit();" class="bu">上报组织机构</button>
  <label id="orgmsg" style="color: red;"></label>
</div>
<div style="margin-top: 30px;">
  <button onclick="getOrg();" class="bu">获取组织机构</button>
  <label id="orgmsg" style="color: red;"></label>
</div>
<hr>

<div style="margin-top: 30px;">
  <dvi>发送公文</dvi>
  <div align="center">
    <table border="1">
      <tr>
        <td>公文标题：</td>
        <td><input type="text" name="title" id="title"
                   style="width: 300px;"></td>
      </tr>
      <!-- <tr>
          <td>收文单位ID：</td>
          <td><input type="text" name="recOrgID" id="recOrgID"
              style="width: 300px;"></td>
      </tr>
      <tr>
          <td>收文单位名称：</td>
          <td><input type="text" name="recOrgName" id="recOrgName"
              style="width: 300px;"></td>
      </tr> -->
    </table>
    <button onclick="sendOrg();" class="bu">发送</button>
    <label id="msg" style="color: red;"></label>
  </div>
</div>
<hr>

<div>
  <div align="center">
    <table border="1">
      <thead>
      <!-- 撤销公文 -->
      <tr>
        <td align="center" colspan="3">撤销公文</td>
      </tr>
      <tr>
        <td>撤销公文detailId：</td>
        <td>
          <input type="text" name="revoked" id="revoked" style="width: 250px;">
        </td>
        <td style="width: 200px;" align="left" rowspan="5">
						<span>
							<button onclick="revoked();" class="bu">撤销</button>
						</span>
          <span style="padding-left: 30px;">
							<label id="revokedMsg" style="color: red;"></label>
						</span>
        </td>
      </tr>

      <tr>
        <td>撤销公文ID：</td>
        <td>
          <input type="text" name="revokedGWID" id="revokedGWID" style="width: 250px;">
        </td>
      </tr>
      <tr>
        <td>公文标题：</td>
        <td><input type="text" name="revokedTitle" id="revokedTitle" style="width: 250px;"></td>
      </tr>
      <tr>
        <td>交换号：</td>
        <td><input type="text" name="revokedExchNo" id="revokedExchNo" style="width: 250px;"></td>
      </tr>
      <tr>
        <td>撤销原因：</td>
        <td>
          <input type="text" name="revokedComment" id="revokedComment" style="width: 250px;">
        </td>
      </tr>
      </thead>

      <!-- 签收公文 -->
      <thead>
      <tr>
        <td align="center" colspan="3">签收公文</td>
      </tr>
      <tr>
        <td>签收公文ID：</td>
        <td>
          <input type="text" name="acceptedId" id="acceptedId" style="width: 250px;">
        </td>
        <td style="width: 200px;" align="left" rowspan="5">
						<span>
							<button onclick="accepted();" class="bu">签收</button>
						</span>
          <span style="padding-left: 30px;">
							<label id="acceptedMsg" style="color: red;"></label>
						</span>
        </td>
      </tr>
      <tr>
        <td>公文标题:</td>
        <td><input type="text" name="acceptedTitle" id="acceptedTitle" style="width: 250px;"></td>
      </tr>
      <tr>
        <td>交换号:</td>
        <td><input type="text" name="acceptedExchNo" id="acceptedExchNo" style="width: 250px;"></td>
      </tr>
      <tr>
        <td>detailId:</td>
        <td><input type="text" name="acceptedDetailId" id="acceptedDetailId" style="width: 250px;"></td>
      </tr>
      <tr>
        <td>签收回复：</td>
        <td>
          <input type="text" name="acceptedComment" id="acceptedComment" style="width: 250px;">
        </td>
      </tr>
      </thead>

      <!-- 回退公文 -->
      <thead>
      <tr>
        <td align="center" colspan="3">回退公文</td>
      </tr>
      <tr>
        <td>回退公文ID：</td>
        <td>
          <input type="text" name="stepBackId" id="stepBackId" style="width: 250px;">
        </td>
        <td style="width: 200px;" align="left" rowspan="5">
						<span>
							<button onclick="stepBack();" class="bu">回退</button>
						</span>
          <span style="padding-left: 30px;">
							<label id="stepBackMsg" style="color: red;"></label>
						</span>
        </td>
      </tr>
      <tr>
        <td>公文标题：</td>
        <td><input type="text" name="stepBackTitle" id="stepBackTitle" style="width: 250px;"></td>
      </tr>
      <tr>
        <td>detailId：</td>
        <td><input type="text" name="stepBackDetailId" id="stepBackDetailId" style="width: 250px;"></td>
      </tr>
      <tr>
        <td>交换号：</td>
        <td><input type="text" name="stepBackExchNo" id="stepBackExchNo" style="width: 250px;"></td>
      </tr>
      <tr>
        <td>回退原因：</td>
        <td>
          <input type="text" name="stepBackComment" id="stepBackComment" style="width: 250px;">
        </td>
      </tr>
      </thead>

    </table>
  </div>
</div>
<hr>
</body>

<script type="text/javascript">

  function getOrg() {
    $.ajax({
      type : "POST",
      url : "${pageContext.request.contextPath}/uploadUnit",
      data : {
        "type" : 'getOrg'
      },
      success : function(msg) {
        var obj = JSON.parse(msg);
        alert(1);

      },
      error : function(msg) {
        alert(msg);
      }
    });
  }


  function uploadUnit() {
    $.ajax({
      type : "POST",
      url : "${pageContext.request.contextPath}/uploadUnit",
      data : {
        "type" : 'org'
      },
      success : function(msg) {
        var obj = JSON.parse(msg);
        if ("success" == obj.msg) {
          $('#orgmsg').html('同步成功');
        } else {
          $('#orgmsg').html('同步失败');
        }

      },
      error : function(msg) {
        alert(msg);
      }
    });
  }

  function sendOrg() {
    $('#msg').html('');
    $.ajax({
      type : "POST",
      url : "${pageContext.request.contextPath}/uploadUnit",
      data : {
        "type" : 'send',
        "title" : $('#title').val()
      },
      success : function(msg) {
        var obj = JSON.parse(msg);
        if ("success" == obj.msg) {
          $('#msg').html('发送公文成功');
        } else {
          $('#msg').html('发送公文失败');
        }
      },
      error : function(msg) {
        alert(msg);
      }
    });
  }

  //撤销公文
  function revoked() {
    $('#revokedMsg').html('');
    $.ajax({
      type : "POST",
      url : "${pageContext.request.contextPath}/uploadUnit",
      data : {
        "type" : 'revoked',
        "revokedId" : $('#revoked').val(),
        "revokedExchNo" : $('#revokedExchNo').val(),
        "title" : $('#revokedTitle').val(),
        "revokedGWID" : $('#revokedGWID').val(),
        "comment":$('#revokedComment').val()
      },
      success : function(msg) {
        var obj = JSON.parse(msg);
        if ("success" == obj.msg) {
          $('#revokedMsg').html('发送成功');
        } else {
          $('#revokedMsg').html('发送失败');
        }
      },
      error : function(msg) {
        alert(msg);
      }
    });
  }
  //签收公文
  function accepted() {
    $('#acceptedMsg').html('');
    $.ajax({
      type : "POST",
      url : "${pageContext.request.contextPath}/uploadUnit",
      data : {
        "type" : 'accepted',
        "acceptedId" : $('#acceptedId').val(),
        "title" : $('#acceptedTitle').val(),
        "exchNo" : $('#acceptedExchNo').val(),
        "detailId" : $('#acceptedDetailId').val(),
        "comment" : $('#acceptedComment').val()
      },
      success : function(msg) {
        var obj = JSON.parse(msg);
        if ("success" == obj.msg) {
          $('#acceptedMsg').html('签收成功');
        } else {
          $('#acceptedMsg').html('签收失败');
        }
      },
      error : function(msg) {
        alert(msg);
      }
    });
  }

  function stepBack() {
    $('#stepBackMsg').html('');
    $.ajax({
      type : "POST",
      url : "${pageContext.request.contextPath}/uploadUnit",
      data : {
        "type" : 'stepBack',
        "stepBackId" : $('#stepBackId').val(),
        "title" : $('#stepBackTitle').val(),
        "detailId" : $('#stepBackDetailId').val(),
        "exchNo" : $('#stepBackExchNo').val(),
        "comment" : $('#stepBackComment').val()
      },
      success : function(msg) {
        var obj = JSON.parse(msg);
        if ("success" == obj.msg) {
          $('#stepBackMsg').html('发送成功');
        } else {
          $('#stepBackMsg').html('发送失败');
        }
      },
      error : function(msg) {
        alert(msg);
      }
    });
  }
</script>
</html>