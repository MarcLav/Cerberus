<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
  ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  ~
  ~ This file is part of Cerberus.
  ~
  ~ Cerberus is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ Cerberus is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/BuildContent.js"></script>
        <title id="pageTitle">Build Content</title>
    </head>
    <body>
        <%@ include file="include/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/buildcontent/addBuildContent.html"%>
            <%@ include file="include/buildcontent/editBuildContent.html"%>
            <%@ include file="include/buildcontent/massActionBuildContent.html"%>
            <%@ include file="include/buildcontent/listInstallInstructions.html"%>

            <h1 class="page-title-line" id="title">Build Content</h1>

            <div class="row">
                <div class="col-lg-12" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card">
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="filters" name="filtersField">Filters</label>
                        </div>
                        <div class="panel-body">
                            <div class="form-inline">
                                <div class="marginBottom10">
                                    <label for="selectApplication" name="applicationField">Application :</label>
                                    <select class="form-control" id="selectApplication" name="application" style="width: 25%"></select>
                                    <label for="selectBuild" name="buildField">Build :</label>
                                    <select class="form-control" id="selectBuild" name="buildf" style="width: 10%"></select>
                                    <label for="selectRevision" name="revisionField">Revision :</label>
                                    <select class="form-control" id="selectRevision" name="revisionf" style="width: 10%"></select>
                                    <button type="button" class="btn btn-default" id="btnLoad" onclick="loadBCTable()" name="btnLoad">Load</button>
                                    <button type="button" class="btn btn-default" id="btnViewInstall" onclick="displayInstallInstructions()" name="btnViewInstall">Preview Install instructions</button>
                                </div>
                                <div class="marginLeft5">
                                    <button type="button" class="btn btn-default" id="btnLoadAll" onclick="setAll()" name="btnLoadAll">Load All Build</button>
                                    <button type="button" class="btn btn-default" id="btnLoadPending" onclick="setPending()" name="btnLoadPending">Load Pending Build</button>
                                    <button type="button" class="btn btn-default" id="btnLoadLatest" onclick="setLatest()" name="btnLoadLatest">Load Latest Build</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading card">
                    <label id="shortcuts" name="listField">Build Content List</label>
                </div>
                <form id="massActionForm" name="massActionForm"  title="MassActionBuidContent" role="form">
                    <div class="panel-body" id="buildContentList">
                        <table id="buildrevisionparametersTable" class="table table-hover display" name="buildrevisionparametersTable"></table>
                        <div class="marginBottom20"></div>
                    </div>
                </form>
            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
