package com.wayapay.thirdpartyintegrationservice.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
@JsonDeserialize
public class NewWalletResponse {
    private Long id;
    private boolean delFlg;
    private boolean entity_cre_flg;
    private String  sol_id;
    private String bacid;
    private String accountNo;
    private String acct_name;
    private String gl_code;
    private String product_code;
    private String acct_ownership;
    private String frez_code;
    private String frez_reason_code;
    private String acct_opn_date;
    private String acct_cls_flg;
    private Double clr_bal_amt;
    private Double un_clr_bal_amt;
    private String hashed_no;
    private boolean int_paid_flg;
    private boolean int_coll_flg;
    private String lchg_user_id;
    private String lchg_time;
    private String rcre_user_id;
    private String rcre_time;
    private String acct_crncy_code;
    private Double lien_amt;
    private String product_type;
    private Double cum_dr_amt;
    private Double cum_cr_amt;
    private boolean chq_alwd_flg;
    private Double cash_dr_limit;
    private Double xfer_dr_limit;
    private Double cash_cr_limit;
    private Double xfer_cr_limit;
    private String acct_cls_date;
    private String last_tran_date;
    private String last_tran_id_dr;
    private String last_tran_id_cr;
    private boolean walletDefault;

    public NewWalletResponse(Long id) {
        this.id = id;
    }
}
