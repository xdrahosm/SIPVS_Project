<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:md="http://www.example.org/sipvs">
    <xsl:output method="html"/>

    <xsl:template match="/md:Library">
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
        <html>
            <head>
                <style type="text/css">
                    .mainblock {
                    width: 840px;
                    padding-bottom: 30px;
                    }

                    .title {
                    padding-left: 300px;
                    }

                    .field {
                    padding-left: 10px;
                    padding-right: 100px;

                    min-width: 150px;
                    display: inline-block;
                    }

                    table {
                    border-collapse: collapse;
                    }

                    tr:nth-child(odd) {
                    background: #F4F4F4;
                    }

                    tr:nth-child(even) {
                    background: #FFFFFF;
                    }

                    th, td {
                    width: 140px;
                    }

                    th {
                    background: #DDDDDD;
                    font-weight: bold;
                    padding-left: 70px;
                    padding-right: 70px;
                    border: 1px black solid;
                    text-align: center;
                    }

                    td {
                    padding-left: 10px;
                    font-size: .8em;
                    }
                </style>
            </head>
            <body>
                <div class="mainblock">
                    <p class="title">Library</p>
                    <div>
                        <div class="field">
                            <p>Book Name</p>
                            <p><xsl:value-of select="//md:BookName"/></p>
                        </div>
                        <div class="field">
                            <p>Author name</p>
                            <p><xsl:value-of select="//md:AuthorName"/></p>
                        </div>
                        <div class="field">
                            <p>Pages</p>
                            <p><xsl:value-of select="//md:Pages"/></p>
                        </div>
                    </div>
                    <div>
                        <div class="field">
                            <p>Printed</p>
                            <p><xsl:value-of select="//md:PrintingYear"/></p>
                        </div>
                        <div class="field">
                            <p>First published</p>
                            <p><xsl:value-of select="//md:FirstPublished"/></p>
                        </div>
                        <div class="field">
                            <p>ISBN</p>
                            <p><xsl:value-of select="//md:ISBN"/></p>
                        </div>
                    </div>
                    <div>
                        <div class="field">
                            <p>Book Description</p>
                            <p><xsl:value-of select="//md:BookDescription"/></p>
                        </div>

                    </div>
                </div>
                <table>
                    <tr>
                        <th>Student Name</th>
                        <th>Borrowed</th>
                        <th>Returned</th>
                    </tr>
                    <xsl:for-each select="//md:BorrowHistory/md:Borrow">
                        <tr>
                            <td>
                                <xsl:value-of select="md:StudentName"/>
                            </td>
                            <td>
                                <xsl:value-of select="md:BorrowDate"/>
                            </td>
                            <td>
                                <xsl:value-of select="md:ReturnDate"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>