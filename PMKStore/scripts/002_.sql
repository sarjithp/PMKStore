update c_tax set rate = 0 where issummary='Y' and ad_client_id > 100;

INSERT INTO C_Tax (C_Tax_ID,AD_Client_ID,AD_Org_ID,Updated,IsDefault,IsActive,Created,C_TaxCategory_ID,CreatedBy,ValidFrom,IsSummary,Rate,Name,UpdatedBy,IsDocumentLevel,RequiresTaxCertificate,IsTaxExempt,IsSalesTax,SOPOType) VALUES (1000082,1000044,0,now(),'N','Y',now(),1000073,100,TO_TIMESTAMP('2018-01-01','YYYY-MM-DD'),'Y',0,'GST 28% + Cess',100,'N','N','N','N','B');

INSERT INTO C_Tax (Parent_Tax_ID,C_Tax_ID,AD_Client_ID,AD_Org_ID,Updated,IsDefault,IsActive,Created,C_TaxCategory_ID,CreatedBy,ValidFrom,IsSummary,Rate,Name,UpdatedBy,IsDocumentLevel,RequiresTaxCertificate,IsTaxExempt,IsSalesTax,SOPOType) VALUES (1000082,1000084,1000044,0,now(),'N','Y',now(),1000073,100,TO_TIMESTAMP('2018-01-01','YYYY-MM-DD'),'N',14,'CGST-14%',100,'N','N','N','N','B');

INSERT INTO C_Tax (Parent_Tax_ID,C_Tax_ID,AD_Client_ID,AD_Org_ID,Updated,IsDefault,IsActive,Created,C_TaxCategory_ID,CreatedBy,ValidFrom,IsSummary,Rate,Name,UpdatedBy,IsDocumentLevel,RequiresTaxCertificate,IsTaxExempt,IsSalesTax,SOPOType) VALUES (1000082,1000085,1000044,0,now(),'N','Y',now(),1000073,100,TO_TIMESTAMP('2018-01-01','YYYY-MM-DD'),'N',14,'SGST-14%',100,'N','N','N','N','B');

INSERT INTO C_Tax (Parent_Tax_ID,C_Tax_ID,AD_Client_ID,AD_Org_ID,Updated,IsDefault,IsActive,Created,C_TaxCategory_ID,CreatedBy,ValidFrom,IsSummary,Rate,Name,UpdatedBy,IsDocumentLevel,RequiresTaxCertificate,IsTaxExempt,IsSalesTax,SOPOType) VALUES (1000082,1000086,1000044,0,now(),'N','Y',now(),1000073,100,TO_TIMESTAMP('2018-01-01','YYYY-MM-DD'),'N',1,'Kerala Flood Cess',100,'N','N','N','N','B');

--INSERT into c_region values (50110,0,0,'Y',now(),100,now(),100,'Calicut','Calicut',208,'Y',null);

UPDATE c_tax set isdefault = 'N' where c_tax_id = 1000079;

UPDATE c_tax set isdefault = 'Y' where c_tax_id = 1000082;

