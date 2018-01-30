package com.hedvig.memberservice.web.dto;

import lombok.Value;
import lombok.experimental.Wither;

import java.util.UUID;
/*        {
        "id": "someid",
        "title": "Rädda Barnen",
        "description": "Lorem ipsum dolor sit amet...",
        "selected": false,
        "charity": true,
        "imageUrl": "https://unsplash.it/400/200"
        },
        {
        "id": "someotherid",
        "title": "Mitt konto",
        "description": "Lorem ipsum dolor sit amet...",
        "selected": true,
        "charity": false,
        "imageUrl": "https://unsplash.it/400/200"
        }
        ]*/

@Value
@Wither
public class CashbackOption {
    public UUID id;
    public String name;
    public String title;
    public String description;
    public Boolean selected;
    public Boolean charity;
    public String imageUrl;
    public String selectedUrl;
    public String signature;
    public String paragraph;
}
